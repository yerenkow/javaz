package org.javaz.test.queues;

import org.javaz.queues.iface.PartialSenderFeedI;

import java.util.Collection;

/**
 * Helper for test, accept lists, prints theirs sizes
 * and throws few times exceptions to make code coverage larger
 */
public class TestFeed implements PartialSenderFeedI
{
    private int throwExceptionCount = 3;
    @Override
    public Collection sendData(Collection list) throws Exception
    {
        if(throwExceptionCount > 0)
        {
            throwExceptionCount--;
            throw new Exception();
        }
        Thread.sleep(40);
        System.out.println("Got list here, " + list.size());
        return list;
    }
}
