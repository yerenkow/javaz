package org.javaz.servlet.test;

import junit.framework.Assert;
import org.javaz.easyssl.AllAllowSslSocketFactory;
import org.javaz.easyssl.UrlFetcher;
import org.junit.Test;

/**
 */
public class TestWebFetch
{
    @Test
    public void testFetch() throws Exception
    {
        new UrlFetcher();
        new AllAllowSslSocketFactory();
        byte[] bytes = UrlFetcher.fetchDataFromUrl("https://javaz.org/");
        Assert.assertTrue(bytes.length > 0);
    }
}
