package org.javaz.jdbc.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default behavior is to use the cache.
 */
public abstract class AbstractJdbcHelper implements JdbcHelperI {
    protected String jdbcAddress = null;
    protected ConnectionProviderI provider = new SimpleConnectionProvider();

    public void setJdbcAddress(String jdbcAddress) {
        this.jdbcAddress = jdbcAddress;
    }

    public String getJdbcAddress() {
        return jdbcAddress;
    }

    public ConnectionProviderI getProvider() {
        return provider;
    }

    public void setProvider(ConnectionProviderI provider) {
        this.provider = provider;
    }

    public abstract Number runUpdate(String query, Map<Integer, Object> parameters);

    public abstract void runUpdateDataIgnore(String query, Map<Integer, Object> parameters);

    public Number runUpdate(StringMapPair pair) {
        return runUpdate(pair.getString(), pair.getMap());
    }

    public void runUpdateDataIgnore(StringMapPair pair) {
        runUpdateDataIgnore(pair.getString(), pair.getMap());
    }

    public abstract ArrayList<List> runMassUpdatePairs(ArrayList<StringMapPair> pairs);

}

