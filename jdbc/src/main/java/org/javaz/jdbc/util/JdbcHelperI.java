package org.javaz.jdbc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public interface JdbcHelperI extends JdbcConstants
{
    /**
     * Getter + setter jdbcAddress
     */
    public void setJdbcAddress(String c);

    public String getJdbcAddress();

    /**
     * How long lists will be in cache before expiration
     */
    public long getListRecordsTtl();

    public void setListRecordsTtl(long listRecordsTtl);

    /**
     * Helper, which can make out Connection from jdbcAddress
     */
    public ConnectionProviderI getProvider();

    public void setProvider(ConnectionProviderI provider);

    /**
     * @param query
     * @param parameters
     * @return List of Maps, corresponding to this request.
     */
    public List getRecordList(String query, Map parameters);

    public List getRecordList(String query, Map parameters, boolean useCache);

    /**
     * Update something in DB
     *
     * @param query
     * @param parameters
     * @return
     */
    public int runUpdate(String query, Map parameters);

    /**
     * Update something in DB, but not expecting nothing back.
     * RETURNING not appended if DB supports this.
     *
     * @param query
     * @param parameters
     * @return
     */
    public void runUpdateDataIgnore(String query, Map parameters);


    /**
     * Run a lots of updates in single Connection
     *
     * @param objects
     * @return
     */
    public ArrayList<List> runMassUpdate(ArrayList<Object[]> objects);
}
