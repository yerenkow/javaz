package org.javaz.jdbc.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Default behavior is to use the cache.
 */
public abstract class AbstractJdbcHelper implements JdbcHelperI
{
    protected String jdbcAddress = null;
    protected long listRecordsTtl = DEFAULT_TTL_LISTS;
    protected ConnectionProviderI provider = new SimpleConnectionProvider();

    public void setJdbcAddress(String jdbcAddress)
    {
        this.jdbcAddress = jdbcAddress;
    }

    public String getJdbcAddress()
    {
        return jdbcAddress;
    }

    public ConnectionProviderI getProvider()
    {
        return provider;
    }

    public void setProvider(ConnectionProviderI provider)
    {
        this.provider = provider;
    }

    public long getListRecordsTtl()
    {
        return listRecordsTtl;
    }

    public void setListRecordsTtl(long listRecordsTtl)
    {
        this.listRecordsTtl = listRecordsTtl;
    }

    public List getRecordList(String query, Map parameters)
    {
        return getRecordList(query, parameters, true);
    }

    public abstract List getRecordList(String query, Map parameters, boolean useCache);

    public abstract int runUpdate(String query, Map parameters);

    public abstract  void runUpdateDataIgnore(String query, Map parameters);

    public abstract ArrayList<List> runMassUpdate(ArrayList<Object[]> objects);

}

