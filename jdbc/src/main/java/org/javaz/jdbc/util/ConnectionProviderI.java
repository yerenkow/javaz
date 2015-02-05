package org.javaz.jdbc.util;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Implemented class should provide java.sqlConnection based on some String.
 */
public interface ConnectionProviderI {
    public Connection getConnection(String dsAddress) throws SQLException;
}
