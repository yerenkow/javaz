package org.javaz.jdbc.util;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class provides java.sqlConnection based on some String.
 * This implementation supports jdbc:... and java:... URLs for SQL.
 */
public class BoneCpConnectionProvider extends SimpleConnectionProvider
{
    private static Logger logger = LogManager.getLogger(BoneCpConnectionProvider.class);

    private static HashMap<String, BoneCP> pools = new HashMap<String, BoneCP>();

    private static Lock lock = new ReentrantLock();

    public int defaultAcquireIncrement = Integer.parseInt(
            System.getProperty("org.javaz.jdbc.util.BoneCpConnectionProvider.defaultAcquireIncrement", "4"));

    public int defaultPartitionCount = Integer.parseInt(
            System.getProperty("org.javaz.jdbc.util.BoneCpConnectionProvider.defaultPartitionCount", "4"));

    public int defaultMaxConnectionsPerPartition = Integer.parseInt(
            System.getProperty("org.javaz.jdbc.util.BoneCpConnectionProvider.defaultMaxConnectionsPerPartition", "10"));

    public int defaultMinConnectionsPerPartition = Integer.parseInt(
            System.getProperty("org.javaz.jdbc.util.BoneCpConnectionProvider.defaultMinConnectionsPerPartition", "1"));

    public boolean disableConnectionTracking = Boolean.parseBoolean(
            System.getProperty("org.javaz.jdbc.util.BoneCpConnectionProvider.disableConnectionTracking", "TRUE"));

    public int getDefaultAcquireIncrement() {
        return defaultAcquireIncrement;
    }

    public void setDefaultAcquireIncrement(int defaultAcquireIncrement) {
        this.defaultAcquireIncrement = defaultAcquireIncrement;
    }

    public int getDefaultPartitionCount() {
        return defaultPartitionCount;
    }

    public void setDefaultPartitionCount(int defaultPartitionCount) {
        this.defaultPartitionCount = defaultPartitionCount;
    }

    public int getDefaultMaxConnectionsPerPartition() {
        return defaultMaxConnectionsPerPartition;
    }

    public void setDefaultMaxConnectionsPerPartition(int defaultMaxConnectionsPerPartition) {
        this.defaultMaxConnectionsPerPartition = defaultMaxConnectionsPerPartition;
    }

    public int getDefaultMinConnectionsPerPartition() {
        return defaultMinConnectionsPerPartition;
    }

    public void setDefaultMinConnectionsPerPartition(int defaultMinConnectionsPerPartition) {
        this.defaultMinConnectionsPerPartition = defaultMinConnectionsPerPartition;
    }

    @Override
    protected Connection getPlainConnection(String dsAddress) throws SQLException {
        if (!pools.containsKey(dsAddress)) {
            try {
                lock.lock();
                if (!pools.containsKey(dsAddress)) {
                    BoneCPConfig config = new BoneCPConfig();
                    config.setJdbcUrl(dsAddress);
                    config.setPartitionCount(defaultPartitionCount);
                    config.setMinConnectionsPerPartition(defaultMinConnectionsPerPartition);
                    config.setMaxConnectionsPerPartition(defaultMaxConnectionsPerPartition);
                    config.setAcquireIncrement(defaultAcquireIncrement);
                    config.setDisableConnectionTracking(disableConnectionTracking);
                    BoneCP connectionPool = new BoneCP(config);
                    pools.put(dsAddress, connectionPool);
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
                pools.get(dsAddress).close();
            }
        } finally {
            lock.unlock();
        }
    }
}
