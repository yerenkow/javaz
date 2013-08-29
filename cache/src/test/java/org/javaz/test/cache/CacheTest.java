package org.javaz.test.cache;

import junit.framework.Assert;
import org.javaz.cache.CacheImpl;
import org.junit.Test;

/**
 *
 */
public class CacheTest
{
    public static String AA = "aa";

    @Test
    public void testCache () throws InterruptedException
    {
        CacheImpl cache = new CacheImpl();
        cache.put(AA, AA);
        Assert.assertEquals(cache.size(), 1);
        int timeToLive = 100;
        cache.setTimeToLive(timeToLive);
        Thread.sleep(101);
        Assert.assertEquals(cache.size(), 0);
        Assert.assertEquals(cache.getTimeToLive(), timeToLive);

        cache.clear();
        Assert.assertFalse(cache.containsKey(null));
        Assert.assertFalse(cache.containsKey(AA));
        Assert.assertFalse(cache.containsValue(AA));
        Assert.assertTrue(cache.isEmpty());

        cache.setTimeToLive(timeToLive*timeToLive);
        cache.put(AA, AA);
        Assert.assertEquals(cache.get(AA), AA);
        Assert.assertEquals(cache.remove(AA), AA);
        Assert.assertTrue(cache.isEmpty());

    }

}
