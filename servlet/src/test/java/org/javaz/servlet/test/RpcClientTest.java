package org.javaz.servlet.test;

import org.apache.xmlrpc.client.XmlRpcClient;
import org.javaz.xmlrpc.GenericRpcFactory;
import org.junit.Ignore;
import org.junit.Test;

import java.io.FileReader;
import java.util.Properties;

/**
 *
 */
public class RpcClientTest
{
    @Test
    @Ignore
    public void testRpc() throws Exception
    {
        new GenericRpcFactory();
        Properties p = new Properties();
        p.load(new FileReader("xmlrpctest.properties"));
        XmlRpcClient client = GenericRpcFactory.getRpcClient(p.getProperty("url"), p.getProperty("user"), p.getProperty("password"));
        Object result = client.execute(p.getProperty("method"), new Object[]{new Object[]{}});
        try
        {
            client.execute("nosuchhmethod", new Object[]{new Object[]{}});
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        XmlRpcClient client2 = GenericRpcFactory.getRpcClient(p.getProperty("url2"), p.getProperty("user2"), p.getProperty("password2"));
        try
        {
            client2.execute(p.getProperty("method2"), new Object[]{new Object[]{}});
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        XmlRpcClient client3 = GenericRpcFactory.getRpcClient(p.getProperty("url3"), p.getProperty("user3"), p.getProperty("password3"));
        try
        {
            client3.execute(p.getProperty("method3"), new Object[]{new Object[]{}});
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
