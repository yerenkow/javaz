package org.javaz.jdbc.util;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface JdbcHelperI extends JdbcConstants {
    /**
     * Getter + setter jdbcAddress
     *
     * @param address to be set
     */
    public void setJdbcAddress(String address);

    public String getJdbcAddress();

    /**
     * @return How long lists will be in cache before expiration
     */
    public long getListRecordsTtl();

    public void setListRecordsTtl(long listRecordsTtl);

    /**
     * @return Helper, which can make out Connection from jdbcAddress
     */
    public ConnectionProviderI getProvider();

    public void setProvider(ConnectionProviderI provider);

    /**
     * @param query      to execute
     * @param parameters with
     * @return List of Maps, corresponding to this request.
     */
    public List getRecordList(String query, Map parameters);

    public List getRecordList(String query, Map parameters, boolean useCache);

    /**
     * Update something in DB
     *
     * @param pair - query to execute with map of params
     * @return count or id
     * @throws SQLException in case something wrong
     */
    public long runUpdate(StringMapPair pair) throws SQLException;

    /**
     * Update something in DB, but not expecting nothing back.
     * RETURNING not appended if DB supports this.
     *
     * @param pair - query to execute with map of params
     */
    public void runUpdateDataIgnore(StringMapPair pair);

    /**
     * Update something in DB
     *
     * @param query      to execute
     * @param parameters with
     * @return count or id
     * @throws SQLException in case something wrong
     */
    public long runUpdate(String query, Map parameters) throws SQLException;

    /**
     * Update something in DB, but not expecting nothing back.
     * RETURNING not appended if DB supports this.
     *
     * @param query      to execute
     * @param parameters with
     */
    public void runUpdateDataIgnore(String query, Map parameters);


    /**
     * Run a lots of updates in single Connection
     *
     * @param objects tuples for updates
     * @return list with counts or ids
     */
    public ArrayList<List> runMassUpdatePairs(ArrayList<StringMapPair> objects);
}
