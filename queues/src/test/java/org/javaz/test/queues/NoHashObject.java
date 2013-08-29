package org.javaz.test.queues;

/**
 *
 */
public class NoHashObject
{
    private String content = null;

    public int hashCode()
    {
        return content.hashCode();
    }
}
