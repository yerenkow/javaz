package org.javaz.jdbc.util;

import java.util.HashMap;

/**
 *
 */
public class JdbcCachedHelper {
    /**
     * This map contains all singleton instances which are used to access DBs.
     * These instances not synchronized, as they not stateful (all activity goes in
     * cache system and/or in DB)
     */
    private static final HashMap<String, JdbcHelperI> jdbcHelperInstances = new HashMap<String, JdbcHelperI>();

    public static JdbcHelperI getInstance(String address)
    {
        if (!jdbcHelperInstances.containsKey(address))
        {
            synchronized (jdbcHelperInstances)
            {
                if (!jdbcHelperInstances.containsKey(address))
                {
                    JdbcHelper jdbcHelper = new JdbcHelper();
                    jdbcHelper.setJdbcAddress(address);
                    jdbcHelperInstances.put(address, jdbcHelper);
                }
            }
        }
        return jdbcHelperInstances.get(address);
    }
}
