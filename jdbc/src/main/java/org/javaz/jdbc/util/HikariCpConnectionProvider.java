package org.javaz.jdbc.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.zaxxer.hikari.HikariDataSource;

/**
 * This class provides java.sqlConnection based on some String.
 * This implementation supports jdbc:... and java:... URLs for SQL.
 */
public class HikariCpConnectionProvider extends SimpleConnectionProvider {
    private static Logger logger = LogManager.getLogger(HikariCpConnectionProvider.class);

    private static HashMap<String, HikariDataSource> pools = new HashMap<String, HikariDataSource>();

    private static Lock lock = new ReentrantLock();

    @Override
    protected Connection getPlainConnection(String dsAddress) throws SQLException {
        if (!pools.containsKey(dsAddress)) {
            try {
                lock.lock();
                if (!pools.containsKey(dsAddress)) {
                    HikariDataSource ds = new HikariDataSource();
                    ds.setJdbcUrl(dsAddress);
                    pools.put(dsAddress, ds);
                }
            } finally {
                lock.unlock();
            }
        }
        return pools.get(dsAddress).getConnection();
    }

    public static void destroyPools() {
        try {
            lock.lock();
            ArrayList<String> dsAddresses = new ArrayList<String>(pools.keySet());
            for (Iterator<String> iterator = dsAddresses.iterator(); iterator.hasNext(); ) {
                String dsAddress = iterator.next();
                logger.debug("Closing connectionPool for " + dsAddress);
                pools.remove(dsAddress).close();
            }
        } finally {
            lock.unlock();
        }
    }
}
