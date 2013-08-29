package org.javaz.queues.iface;

import java.util.Collection;

/**
 *
 */
public interface PartialSenderFeedI
{
    /**
     * @param list of values to be sent, data came here by chunks
     * @return some result, if supported
     * @throws Exception
     */
    public Collection sendData(Collection list) throws Exception;
}
