package org.javaz.jdbc.util;

import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    public static final ConnectionProviderFactory defaultFactory = new ConnectionProviderFactory();
    public static final ReadWriteLock lock = new ReentrantReadWriteLock();

    public static JdbcHelperI getInstance(String address) {
        return getInstance(address, defaultFactory);
    }

    public static JdbcHelperI getInstance(String address, ConnectionProviderFactory factory) {
        final String key = factory + "_" + address;
        if (!jdbcHelperInstances.containsKey(key)) {
                lock.writeLock().lock();
                try {
                    if (!jdbcHelperInstances.containsKey(key)) {
                        JdbcHelper jdbcHelper = new JdbcHelper();
                        jdbcHelper.setJdbcAddress(address);
                        jdbcHelper.setProvider(factory.createProvider(address));
                        jdbcHelperInstances.put(key, jdbcHelper);
                    }
                } finally {
                    lock.writeLock().unlock();
                }
        }
        return jdbcHelperInstances.get(key);
    }

    public static void destroyInstance(String address, ConnectionProviderFactory factory) {
        final String key = factory + "_" + address;
        lock.writeLock().lock();
        try {
            jdbcHelperInstances.remove(key);
        } finally {
            lock.writeLock().unlock();
        }

    }
}
