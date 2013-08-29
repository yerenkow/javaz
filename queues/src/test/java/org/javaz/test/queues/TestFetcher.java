package org.javaz.test.queues;

import org.javaz.queues.iface.RecordsFetcherI;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 
 */
public class TestFetcher implements RecordsFetcherI
{
    private int STATE_STARTED_1 = 1;
    private int STATE_DONE_1 = 2;
    private int STATE_STARTED_2 = 3;
    private int STATE_DONE_2 = 4;
    private int STATE_STARTED_3 = 5;
    private int STATE_DONE_3 = 6;

    private int state = STATE_STARTED_1;

    @Override
    public Object[] getMinMaxBounds()
    {

        if(state == STATE_STARTED_1)
        {
            return new Object[]{1, 1, 1};
        }

        if(state == STATE_DONE_1)
        {
            state = STATE_STARTED_2;
            return new Object[]{1, 1000, 10000};
        }
        if(state == STATE_DONE_2)
        {
            state = STATE_STARTED_3;
            return new Object[]{100, 100, 1};
        }
        return new Object[]{null, null};
    }

    @Override
    public Object[] getRecordsArray(long offset, long limit)
    {
        Collection collection = getRecordsCollection(offset, limit);
        return collection.toArray(new Object[collection.size()]);
    }

    @Override
    public Collection getRecordsCollection(long offset, long limit)
    {
        ArrayList arrayList = new ArrayList();
        if(state == STATE_STARTED_1)
        {
            state = STATE_DONE_1;
            arrayList.add("a");
        }

        if(state == STATE_STARTED_2)
        {
            state = STATE_DONE_2;
            arrayList.add("a");
            arrayList.add("b");
            arrayList.add("c");
        }

        if(state == STATE_STARTED_3)
        {
            state = STATE_DONE_3;
            arrayList.add(new NoHashObject());
        }

        return arrayList;
    }

    @Override
    public String getDescriptiveName()
    {
        return "Test fetcher";
    }
}
