package org.javaz.xmlrpc;

import org.apache.http.client.HttpClient;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.apache.xmlrpc.client.XmlRpcTransportFactoryImpl;

public class XmlRpcHttpClient4TransportFactory extends XmlRpcTransportFactoryImpl
{

    private HttpClient httpClient;

    public XmlRpcHttpClient4TransportFactory(XmlRpcClient pClient)
    {
        super(pClient);
    }

    @Override
    public XmlRpcTransport getTransport()
    {
        return new XmlRpcHttpClient4Transport(this);
    }

    public void setHttpClient(HttpClient pHttpClient)
    {
        httpClient = pHttpClient;
    }

    public HttpClient getHttpClient()
    {
        return httpClient;
    }

}
