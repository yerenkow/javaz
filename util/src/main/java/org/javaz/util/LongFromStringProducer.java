package org.javaz.util;

/**
 */
public class LongFromStringProducer extends SubObjectProducerI
{
    @Override
    public Comparable getPartForComparing(Object in)
    {
        return new Long((String) in);
    }
}
