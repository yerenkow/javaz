package org.javaz.queues.iface;

import java.util.Collection;

/**
 *
 */
public interface RecordsRotatorI extends Runnable
{
    public void stop();

    public Collection getLogs();

    public Object getNextElement();

    public int getCurrentQueueSize();

    public Collection getManyElements(int size);

    public String getFetcherDescriptiveName();

    public int getMaxLogsCount();

    public void setMaxLogsCount(int logsCount);

    public int getMinSize();

    public void setMinSize(int minSize);

    public int getFetchSize();

    public void setFetchSize(int fetchSize);

    public int getFetchDelay();

    public void setFetchDelay(int fetchDelay);

    public int getFillTries();

    public void setFillTries(int fillTries);

    public int getNoDataDelay();

    public void setNoDataDelay(int noDataDelay);

    public int getInsufficientDataDelay();

    public void setInsufficientDataDelay(int insufficientDataDelay);

    public int getMaxBulksCount();

    public void setMaxBulksCount(int maxBulksCount);
}
