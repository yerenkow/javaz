package org.javaz.util;

import java.util.Map;

/**
 */
public class MapValueProducer extends SubObjectProducerI
{
    private Object key = null;

    public MapValueProducer(String key)
    {
        this.key = key;
    }

    public Object getKey()
    {
        return key;
    }

    public void setKey(Object key)
    {
        this.key = key;
    }

    public Comparable getPartForComparing(Object in)
    {
        Object obj = ((Map) in).get(key);

        return nested != null ? nested.getPartForComparing(obj) : (Comparable) obj;
    }
}
