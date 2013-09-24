package org.javaz.jdbc.queues;

import org.javaz.jdbc.util.ConnectionProviderI;
import org.javaz.jdbc.util.JdbcConstants;
import org.javaz.jdbc.util.SimpleConnectionProvider;
import org.javaz.jdbc.util.UnsafeSqlHelper;
import org.javaz.queues.iface.PartialSenderFeedI;
import org.javaz.queues.impl.SimplePartialSender;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class GenericDbUpdater extends SimplePartialSender
{

    public static String DEFAULT_MAX_UPDATE_THREADS = "16";
    public static String DEFAULT_SHORT_SEND_PERIOD = "100";
    public static String DEFAULT_LONG_SEND_PERIOD = "6000";
    public static String DEFAULT_MAX_OBJECTS_PER_UPDATE = "5120";


    /**
     * How much threads will run simultaneously and run some updates
     */
    public static int MAX_UPDATE_THREADS =
            Integer.valueOf(System.getProperty("org.javaz.jdbc.queues.MAX_UPDATE_THREADS", DEFAULT_MAX_UPDATE_THREADS)).intValue();

    /**
     * If we just updated something, how long we will sleep before trying
     * to find next data
     */
    public static int SHORT_SEND_PERIOD =
            Integer.valueOf(System.getProperty("org.javaz.jdbc.queues.SHORT_SEND_PERIOD", DEFAULT_SHORT_SEND_PERIOD)).intValue();

    /**
     * If there's nothing update at all - let's sleep a bit longer
     */
    public static int LONG_SEND_PERIOD =
            Integer.valueOf(System.getProperty("org.javaz.jdbc.queues.LONG_SEND_PERIOD", DEFAULT_LONG_SEND_PERIOD)).intValue();

    /**
     * Most JDBC can't handle more than 30k placeholders - "?"
     * This should be pretty big, to have considerable effect
     */
    public static int MAX_OBJECTS_PER_UPDATE =
            Integer.valueOf(System.getProperty("org.javaz.jdbc.queues.MAX_OBJECTS_PER_UPDATE", DEFAULT_MAX_OBJECTS_PER_UPDATE)).intValue();

    private static final HashMap<String, HashMap> queryQueues = new HashMap<String, HashMap>();

    private static boolean running = false;

    public static ConnectionProviderI providerI = new SimpleConnectionProvider();

    private ExecutorService service = null;

    private GenericDbUpdater(PartialSenderFeedI senderFeedI)
    {
        super(senderFeedI);
    }

    private void initPool()
    {
        service = Executors.newFixedThreadPool(MAX_UPDATE_THREADS);
    }

    //NOT running by thread, just here to hold DB ID AND query.
    private static final HashMap<String, GenericDbUpdater> fakes = new HashMap<String, GenericDbUpdater>();

    public static GenericDbUpdater getInstance(String query, String db)
    {
        if (!running)
        {
            synchronized (queryQueues)
            {
                if (!running)
                {
                    running = true;
                    GenericDbUpdater instance = new GenericDbUpdater(null);
                    instance.setSendPeriod(SHORT_SEND_PERIOD);
                    instance.initPool();
                    Thread thread = new Thread(instance);
                    thread.start();
                }
            }
        }

        String keyCalculated = db + "#" + query.hashCode();
        if (fakes.containsKey(keyCalculated))
        {
            return fakes.get(keyCalculated);
        }
        GenericDbUpdater fake = new GenericDbUpdater(null);
        fake.setDb(db);
        fake.setQuery(query);
        fakes.put(keyCalculated, fake);

        return fake;
    }

    public void addToQueue(Object o)
    {
        synchronized (queryQueues)
        {
            if (!queryQueues.containsKey(db))
            {
                queryQueues.put(db, new HashMap());
            }

            HashMap map = queryQueues.get(db);
            if (!map.containsKey(query))
            {
                map.put(query, new ArrayList());
            }

            ((ArrayList) map.get(query)).add(o);
        }
    }

    public void addToQueueAll(Collection c)
    {
        synchronized (queryQueues)
        {
            if (!queryQueues.containsKey(db))
            {
                queryQueues.put(db, new HashMap());
            }

            HashMap map = queryQueues.get(db);
            if (!map.containsKey(query))
            {
                map.put(query, new ArrayList());
            }

            ((ArrayList) map.get(query)).addAll(c);
        }
    }

    public void run()
    {
        while (running)
        {
            runDbUpdates();
            try
            {
                Thread.sleep(getSendPeriod());
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }


    public void runDbUpdates()
    {
        boolean noDataToUpdate = false;
        //this synchronized code blocks only for brief time - when thread tries to find something to update
        synchronized (queryQueues)
        {
            Set<String> dbs = queryQueues.keySet();
            for (Iterator<String> iterator = dbs.iterator(); iterator.hasNext(); )
            {
                String db = iterator.next();
                HashMap queryObject = queryQueues.get(db);
                ArrayList queriesIterator = new ArrayList(queryObject.keySet());
                for (Iterator iterator1 = queriesIterator.iterator(); iterator1.hasNext(); )
                {
                    String queryUpdate = (String) iterator1.next();
                    ArrayList tmpList = (ArrayList) queryObject.get(queryUpdate);
                    if (!tmpList.isEmpty())
                    {
                        List list = new ArrayList();
                        list.addAll(tmpList);
                        tmpList.clear();

                        service.execute(new GenericDbUpdaterThread(db, queryUpdate, list, providerI));
                        noDataToUpdate = true;
                    }
                    queryObject.remove(queryUpdate);
                }
            }
        }

        if (noDataToUpdate)
        {
            //if there really nothing to update - let's sleep for a bit more.
            setSendPeriod(LONG_SEND_PERIOD);
            return;
        }

        setSendPeriod(SHORT_SEND_PERIOD);
    }

    private String query = null;
    private String db = null;

    public String getQuery()
    {
        return query;
    }

    public void setQuery(String query)
    {
        this.query = query;
    }

    public String getDb()
    {
        return db;
    }

    public void setDb(String db)
    {
        this.db = db;
    }
}
