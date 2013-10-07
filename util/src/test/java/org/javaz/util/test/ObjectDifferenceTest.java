package org.javaz.util.test;

import junit.framework.Assert;
import org.javaz.util.ObjectDifference;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;

/**
 */
public class ObjectDifferenceTest
{
    @Test
    public void testHashPacking()
    {
        ArrayList listOfHashes = new ArrayList();
        int testSize = 1000;
        for (int i = 0; i < testSize; i++)
        {

            HashMap hashMap = new HashMap();
            hashMap.put("kv", "name_" + i);
            hashMap.put("other", "name_" + (testSize - i));
            hashMap.put("value", i);

            listOfHashes.add(hashMap);
        }

        HashMap kv = ObjectDifference.packHashesByKey(listOfHashes, "kv");
        HashMap other = ObjectDifference.packHashesByKey(listOfHashes, "other");
        Assert.assertEquals(kv.size(), other.size());
        Assert.assertEquals(kv.size(), listOfHashes.size());

        Assert.assertEquals(kv.size(), listOfHashes.size());

        for (int i = 0; i < testSize; i++)
        {
            HashMap map1 = (HashMap) kv.get("name_" + i);
            HashMap map2 = (HashMap) other.get("name_" + (testSize - i));
            Assert.assertEquals(map1, map2);
            HashMap map3 = (HashMap) listOfHashes.get(i);

            Assert.assertEquals(map1, map3);
        }

    }

    @Test
    public void testDiff()
    {
        HashMap h1 = new HashMap();
        h1.put("a", "a");
        h1.put("b", "b");
        h1.put("c", "c");
        h1.put("d", "X");

        HashMap h2 = new HashMap();
        h2.put("a", "a");
        // h2.put("b", "b");
        h2.put("c", "c");
        h2.put("d", "Y");
        h2.put("e", "e");

        HashMap inANotInB = ObjectDifference.getInANotInB(h1, h2);
        Assert.assertEquals(inANotInB.size(), 1);
        Assert.assertEquals(inANotInB.get("b"), "b");

        HashMap inAAndInB = ObjectDifference.getInAAndInB(h1, h2);
        Assert.assertEquals(inAAndInB.size(), 3);
        Assert.assertEquals(inAAndInB.get("d"), h1.get("d"));

        HashMap inAAndInBEquals = ObjectDifference.getInAAndInBEquals(h1, h2);
        Assert.assertEquals(inAAndInBEquals.size(), 2);
        Assert.assertEquals(inAAndInBEquals.get("a"), "a");

        HashMap inBNotInA = ObjectDifference.getInANotInB(h2, h1);
        Assert.assertEquals(inBNotInA.size(), 1);
        Assert.assertEquals(inBNotInA.get("e"), "e");

        HashMap inBAndInA = ObjectDifference.getInAAndInB(h2, h1);
        Assert.assertEquals(inBAndInA.size(), 3);
        Assert.assertEquals(inBAndInA.get("d"), h2.get("d"));

        HashMap inBAndInAEquals = ObjectDifference.getInAAndInBEquals(h2, h1);
        Assert.assertEquals(inBAndInAEquals.size(), 2);
        Assert.assertEquals(inBAndInAEquals.get("a"), "a");


        Assert.assertEquals(ObjectDifference.getInAAndInB(h1, h1).size(), h1.size());
        Assert.assertEquals(ObjectDifference.getInAAndInBEquals(h1, h1).size(), h1.size());
        Assert.assertEquals(ObjectDifference.getInANotInB(h1, h1).size(), 0);

    }
}
