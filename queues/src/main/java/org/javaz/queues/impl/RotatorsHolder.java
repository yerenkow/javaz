package org.javaz.queues.impl;

import org.javaz.queues.iface.RecordsFetcherI;
import org.javaz.queues.iface.RecordsRotatorI;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 */
public class RotatorsHolder
{

    protected static final HashMap<Integer, RecordsRotatorI> rotatersByParameters = new HashMap<Integer, RecordsRotatorI>();

    public static ArrayList<RecordsRotatorI> getAllRotatersForDebug()
    {
        return new ArrayList<RecordsRotatorI>(rotatersByParameters.values());
    }

    public static RecordsRotatorI getRotater(RecordsFetcherI key)
    {
        if (rotatersByParameters.containsKey(key.hashCode()))
        {
            return (RecordsRotatorI) rotatersByParameters.get(key.hashCode());
        }
        synchronized (rotatersByParameters)
        {
            if (rotatersByParameters.containsKey(key.hashCode()))
            {
                return (RecordsRotatorI) rotatersByParameters.get(key.hashCode());
            }

            SimpleRecordsRotator rotater = new SimpleRecordsRotator(key);
            new Thread(rotater).start();

            rotatersByParameters.put(key.hashCode(), rotater);
            return (RecordsRotatorI) rotatersByParameters.get(key.hashCode());
        }
    }


}

