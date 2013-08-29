package org.javaz.queues.impl;

import org.javaz.queues.iface.RecordsFetcherI;
import org.javaz.queues.iface.RecordsRotatorI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 */
public class SimpleRecordsRotator implements RecordsRotatorI
{
    protected RecordsFetcherI objectFetcher = null;
    protected ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
    protected static final double ROUGH_HASH_SIZE = 1.4;

    //default values, can be changed in any time
    protected int minSize = 1000;
    protected int fetchDelay = 60000;
    protected int noDataDelay = 300000;
    protected int insufficientDataDelay = 600000;
    protected int fetchSize = 100;
    protected int fillTries = 5;
    protected int maxBulksCount = 25;
    protected int runThroughDataSize = 0;
    protected boolean running = true;
    protected boolean runThroughNotFilled = false;
    protected boolean fetchAll = false;

    protected int maxLogsCount = 32;
    protected ArrayList<Object[]> logs = new ArrayList<Object[]>();
    protected long startWhenIteration = 0l;

    protected Long min = null;
    protected Long max = null;
    protected Long current = null;

    public SimpleRecordsRotator(RecordsFetcherI objectFetcher)
    {
        this.objectFetcher = objectFetcher;
    }

    public void stop()
    {
        running = false;
    }

    public int getMaxLogsCount()
    {
        return maxLogsCount;
    }

    public void setMaxLogsCount(int maxLogsCount)
    {
        this.maxLogsCount = maxLogsCount;
    }

    public Collection getLogs()
    {
        return logs;
    }

    public void run()
    {
        while (running)
        {
            if (queue.size() < minSize)
            {
                runRefill();
            }
            Thread.yield();
            try
            {
                if (runThroughNotFilled)
                {
                    runThroughNotFilled = false;
                    Thread.sleep(insufficientDataDelay);
                }
                else
                {
                    Thread.sleep(min == null ? noDataDelay : fetchDelay);
                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    protected void runRefill()
    {
        boolean completedRound = (current != null && max != null && current > max);
        boolean needGetMinMax = (min == null || completedRound);
        if (needGetMinMax)
        {
            //get min & max
            fetchAll = false;
            if (startWhenIteration != 0)
            {
                if (logs.size() > maxLogsCount)
                {
                    logs.remove(0);
                }
                logs.add(new Object[]{startWhenIteration, System.currentTimeMillis()});
            }

            startWhenIteration = System.currentTimeMillis();

            Object[] minMaxBounds = objectFetcher.getMinMaxBounds();
            if (minMaxBounds != null && minMaxBounds.length >= 2)
            {
                Object minBound = minMaxBounds[0];
                Object maxBound = minMaxBounds[1];
                if (minBound == null || maxBound == null)
                {
                    min = null;
                }
                else
                {
                    min = ((Number) minBound).longValue();
                    max = ((Number) maxBound).longValue();
                    if (minMaxBounds.length == 3)
                    {
                        Number count = (Number) minMaxBounds[2];
                        if (count != null && count.longValue() <= minSize)
                        {
                            fetchAll = true;
                        }
                    }
                    current = min;
                    if (!fetchAll)
                    {
                        //try to autoScale
                        try
                        {
                            long bulks = Math.abs(max - min) / fetchSize;
                            if (maxBulksCount > 1 && bulks > maxBulksCount)
                            {
                                //guaranteed to be 1 or more by condition above
                                fetchSize = (int) (Math.abs(max - min) / maxBulksCount);
                            }
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (completedRound && runThroughDataSize <= minSize)
            {
                //we are in situation, when running through all queue didn't satisfy us.
                //in this case we need some delay, before try to fill it all again.
                runThroughNotFilled = true;
            }
            runThroughDataSize = 0;
        }

        if (!runThroughNotFilled && min != null)
        {
            try
            {
                boolean needEnd = false;

                HashMap<Integer, Integer> hashCodes = new HashMap<Integer, Integer>((int) (ROUGH_HASH_SIZE * queue.size()));
                Iterator it = queue.iterator();
                while (it.hasNext())
                {
                    Object queueElement = it.next();
                    hashCodes.put(queueElement.hashCode(), queueElement.hashCode());
                }

                int tries = fillTries;
                while (!needEnd && tries-- > 0)
                {
                    Collection collection = null;
                    if (fetchAll)
                    {
                        //don't forget +1 here
                        collection = objectFetcher.getRecordsCollection(min, max - min + 1);
                    }
                    else
                    {
                        collection = objectFetcher.getRecordsCollection(current, fetchSize);
                    }
                    if (collection != null && !collection.isEmpty())
                    {
                        for (Iterator iterator = collection.iterator(); iterator.hasNext(); )
                        {
                            Object o = iterator.next();
                            try
                            {
                                if (o != null && !hashCodes.containsKey(o.hashCode()))
                                {
                                    queue.offer(o);
                                    runThroughDataSize++;
                                    try
                                    {
                                        hashCodes.put(o.hashCode(), o.hashCode());
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            catch (Exception e)
                            {
                                e.printStackTrace();
                                queue.offer(o);
                                runThroughDataSize++;
                                try
                                {
                                    hashCodes.put(o.hashCode(), o.hashCode());
                                }
                                catch (Exception e1)
                                {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }
                    current += fetchSize;

                    needEnd = (queue.size() >= minSize) || current > max;
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public int getMinSize()
    {
        return minSize;
    }

    public void setMinSize(int minSize)
    {
        this.minSize = minSize;
    }

    public int getFetchSize()
    {
        return fetchSize;
    }

    public void setFetchSize(int fetchSize)
    {
        if(fetchSize > 0)
        {
            this.fetchSize = fetchSize;
        }
    }

    public int getFetchDelay()
    {
        return fetchDelay;
    }

    public void setFetchDelay(int fetchDelay)
    {
        if(fetchDelay > 0)
        {
            this.fetchDelay = fetchDelay;
        }
    }

    public int getFillTries()
    {
        return fillTries;
    }

    public void setFillTries(int fillTries)
    {
        if(fillTries > 0)
        {
            this.fillTries = fillTries;
        }
    }

    public int getNoDataDelay()
    {
        return noDataDelay;
    }

    public void setNoDataDelay(int noDataDelay)
    {
        if(fillTries > 0)
        {
            this.noDataDelay = noDataDelay;
        }
    }

    public int getInsufficientDataDelay()
    {
        return insufficientDataDelay;
    }

    public void setInsufficientDataDelay(int insufficientDataDelay)
    {
        this.insufficientDataDelay = insufficientDataDelay;
    }

    public int getMaxBulksCount()
    {
        return maxBulksCount;
    }

    public void setMaxBulksCount(int maxBulksCount)
    {
        if(maxBulksCount > 0)
        {
            this.maxBulksCount = maxBulksCount;
        }
    }

    public Object getNextElement()
    {
        if (!queue.isEmpty())
        {
            return queue.poll();
        }
        return null;
    }

    public int getCurrentQueueSize()
    {
        return queue.size();
    }

    public Collection getManyElements(int size)
    {
        ArrayList l = new ArrayList();
        for (int i = 0; i < size; i++)
        {
            Object poll = queue.poll();
            if (poll == null)
            {
                return l;
            }
            l.add(poll);
        }
        return l;
    }

    public String toString()
    {
        return "RecordsRotator{" +
                "startWhenIteration=" + startWhenIteration +
                ", min=" + min +
                ", max=" + max +
                ", current=" + current +
                ", queue=" + queue.size() +
                ", minSize=" + minSize +
                ", runThroughDataSize=" + runThroughDataSize +
                ", fetchDelay=" + fetchDelay +
                ", noDataDelay=" + noDataDelay +
                ", insufficientDataDelay=" + insufficientDataDelay +
                ", fetchSize=" + fetchSize +
                ", fillTries=" + fillTries +
                ", running=" + running +
                ", maxLogsCount=" + maxLogsCount +
                ", logs=" + logs +
                '}';
    }

    public String getFetcherDescriptiveName()
    {
        return objectFetcher.getDescriptiveName();
    }
}
