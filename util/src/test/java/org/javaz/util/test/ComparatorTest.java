package org.javaz.util.test;

import org.javaz.util.GenericDeepComparator;
import org.javaz.util.LongFromStringProducer;
import org.javaz.util.MapValueProducer;
import org.javaz.util.SplitStringProducer;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ComparatorTest
{
    @Test
    public void testComparators()
    {
        ArrayList list = new ArrayList();
        for (int i = 1; i < 4; i++)
        {
            HashMap map = new HashMap();
            map.put("a", "i" + i + "\t" + i);
            list.add(map);
        }

        {
            HashMap map = new HashMap();
            map.put("a", "i" + 9 + "\t" + -1);
            list.add(map);
        }

        {
            HashMap map = new HashMap();
            map.put("a", "i" + 0 + "\t" + 1);
            list.add(map);
        }

        {
            HashMap map = new HashMap();
            map.put("b", "b");
            list.add(map);
        }

        GenericDeepComparator deepComparator = new GenericDeepComparator();
        MapValueProducer producer1 = new MapValueProducer("a");
        SplitStringProducer nested1 = new SplitStringProducer("\t", 1);
        nested1.setNested(new LongFromStringProducer());
        producer1.setNested(nested1);
        deepComparator.setProducerI(producer1);


        GenericDeepComparator secondarySort = new GenericDeepComparator();
        MapValueProducer producer2 = new MapValueProducer("a");
        producer2.setNested(new SplitStringProducer("\t", 0));
        secondarySort.setProducerI(producer2);
        deepComparator.setSecondarySort(secondarySort);


        Assert.assertEquals(list.size(), 6);
        Collections.shuffle(list);
        Collections.sort(list, deepComparator);
        Assert.assertEquals(list.size(), 6);

        Map pos0 = (Map) list.get(0);
        Map pos1 = (Map) list.get(1);
        Map pos2 = (Map) list.get(2);

        Assert.assertEquals(pos0.get("a"), "i9\t-1");
        Assert.assertEquals(pos1.get("a"), "i0\t1");
        Assert.assertEquals(pos2.get("a"), "i1\t1");

        deepComparator.setInverted(true);
        secondarySort.setInverted(true);
        Collections.sort(list, deepComparator);

        pos0 = (Map) list.get(0);
        pos1 = (Map) list.get(1);
        pos2 = (Map) list.get(2);
        Map pos3 = (Map) list.get(3);
        Map pos4 = (Map) list.get(4);
        Map pos5 = (Map) list.get(5);

        Assert.assertEquals(pos0.get("b"), "b");
        Assert.assertEquals(pos1.get("a"), "i3\t3");
        Assert.assertEquals(pos2.get("a"), "i2\t2");
        Assert.assertEquals(pos3.get("a"), "i1\t1");
        Assert.assertEquals(pos4.get("a"), "i0\t1");
        Assert.assertEquals(pos5.get("a"), "i9\t-1");

        secondarySort.setInverted(false);
        Collections.sort(list, deepComparator);

        pos3 = (Map) list.get(3);
        pos4 = (Map) list.get(4);
        pos5 = (Map) list.get(5);

        Assert.assertEquals(pos3.get("a"), "i0\t1");
        Assert.assertEquals(pos4.get("a"), "i1\t1");
        Assert.assertEquals(pos5.get("a"), "i9\t-1");


    }
}
