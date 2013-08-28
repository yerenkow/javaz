package org.javaz.queues.iface;

import java.util.Collection;

/**
 *
 */
public interface RecordsFetcherI
{
    /**
     *
     * @return {min, max}
     */
    public Object[] getMinMaxBounds();

    /**
     *
     * @param offset - start
     * @param limit - limit of records
     * @return array of records
     */
	public Object[] getRecordsArray(long offset, long limit);


    /**
     *
     * @param offset - start
     * @param limit - limit of records
     * @return collection of records
     */
	public Collection getRecordsCollection(long offset, long limit);


    /**
     *
     * @return descriptive name, to be used in some logging
     */
	public String getDescriptiveName();
}
