package org.javaz.xmlrpc;

import java.net.URL;
import java.util.HashMap;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.javaz.easyssl.UrlFetcher;

public class GenericRpcFactory
{
    public static int HTTP_SO_TIMEOUT = 120000;
    public static int HTTP_CONNECTION_TIMEOUT = 120000;

    public static HashMap poolsByUrl = new HashMap();

    public static XmlRpcClient getRpcClient(String rpcUrl, String username, String password) throws Exception
    {
        return getRpcClient(rpcUrl, username, password, HTTP_CONNECTION_TIMEOUT, HTTP_SO_TIMEOUT);
    }

    public static XmlRpcClient getRpcClient(String rpcUrl, String username, String password, int timeout, int soTimeout) throws Exception
    {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        if (username != null)
            config.setBasicUserName(username);
        if (username != null && password != null)
            config.setBasicPassword(password);
        URL url = new URL(rpcUrl);
        config.setServerURL(url);
        config.setEnabledForExceptions(true);
        config.setEnabledForExtensions(true);

        if (!poolsByUrl.containsKey(rpcUrl))
        {
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            PoolingClientConnectionManager poolingClientConnectionManager = new PoolingClientConnectionManager(schemeRegistry);
            poolsByUrl.put(rpcUrl, poolingClientConnectionManager);

            poolingClientConnectionManager.setMaxTotal(100);
        }

        //Manages a pool of client connections and is able to service connection requests from multiple execution threads
        PoolingClientConnectionManager poolingClientConnectionManager = (PoolingClientConnectionManager) poolsByUrl.get(rpcUrl);
        HttpClient httpClient = new DefaultHttpClient(poolingClientConnectionManager);
        UrlFetcher.configureSSLHandling(httpClient);
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeout);

        org.apache.xmlrpc.client.XmlRpcClient client = new org.apache.xmlrpc.client.XmlRpcClient();
        XmlRpcHttpClient4TransportFactory xmlRpcHttpClient4TransportFactory = new XmlRpcHttpClient4TransportFactory(client);
        xmlRpcHttpClient4TransportFactory.setHttpClient(httpClient);
        client.setTransportFactory(xmlRpcHttpClient4TransportFactory);
        client.setConfig(config);

        return client;
    }
}
