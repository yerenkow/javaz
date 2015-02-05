package org.javaz.jdbc.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * This class provides java.sqlConnection based on some String.
 * This implementation supports jdbc:... and java:... URLs for SQL.
 */
public class SimpleConnectionProvider implements ConnectionProviderI, JdbcConstants {
    private static Logger logger = LogManager.getLogger(SimpleConnectionProvider.class);

    public Connection getConnection(String dsAddress) throws SQLException {
        if (dsAddress.startsWith(JDBC_MARKER)) {
            return getPlainConnection(dsAddress);
        }

        return getJndiConnection(dsAddress);
    }

    protected Connection getPlainConnection(String dsAddress) throws SQLException {
        return DriverManager.getConnection(dsAddress);
    }

    protected Connection getJndiConnection(String dsAddress) throws SQLException {
        if (dsAddress.startsWith(JAVA_MARKER)) {
            InitialContext context = null;
            try {
                context = new InitialContext();
            } catch (NamingException e) {
                logger.error(e);
                return null;
            }
            DataSource ds;
            try {
                ds = (DataSource) context.lookup(dsAddress);
            } catch (NamingException e) {
                logger.error(e);
                return null;
            }
            return ds.getConnection();
        }
        return null;
    }
}
