package org.javaz.jdbc.util;

import org.javaz.cache.CacheI;
import org.javaz.cache.CacheImpl;

import java.util.*;

/**
 *
 */
public class JdbcHelper extends AbstractJdbcHelper
{

    private final CacheI jdbcCacheLists = new CacheImpl();

    protected JdbcHelper()
    {
        jdbcCacheLists.setTimeToLive(DEFAULT_TTL_LISTS);
    }

    public ArrayList<List> runMassUpdate(ArrayList<Object[]> objects)
    {
        return UnsafeSqlHelper.runMassSqlUnsafe(getProvider(), jdbcAddress, objects);
    }

    public int runUpdate(String query, Map parameters)
    {
        ArrayList list = null;
        try
        {
            list = UnsafeSqlHelper.runSqlUnsafe(getProvider(), jdbcAddress, query, ACTION_EXECUTE_UPDATE, parameters);
            if (list != null && !list.isEmpty())
            {
                Object object = list.get(0);
                if (object != null && object instanceof Number)
                {
                    return ((Number) object).intValue();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return 0;
    }

    public List getRecordList(String query, Map parameters, boolean useCache)
    {
        Object o = null;
        StringBuilder cacheKey = null;
        if (useCache)
        {
            cacheKey = new StringBuilder();

            cacheKey.append(jdbcAddress).append("_").append(query.hashCode());
            if (parameters != null)
            {
                Set set = parameters.keySet();
                for (Iterator iterator = set.iterator(); iterator.hasNext(); )
                {
                    Object x = iterator.next();
                    cacheKey.append("_");
                    cacheKey.append(("" + x).hashCode());
                    cacheKey.append("_");
                    cacheKey.append(("" + parameters.get(x)).hashCode());
                }
            }
            o = jdbcCacheLists.get(cacheKey.toString());
        }
        if (o == null)
        {
            try
            {
                List list = UnsafeSqlHelper.runSqlUnsafe(getProvider(), jdbcAddress, query, ACTION_MAP_RESULTS_SET, parameters);
                if (useCache && list != null)
                {
                    synchronized (jdbcCacheLists)
                    {
                        jdbcCacheLists.put(cacheKey.toString(), list);
                    }
                }
                return list;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        return (List) o;
    }
}
