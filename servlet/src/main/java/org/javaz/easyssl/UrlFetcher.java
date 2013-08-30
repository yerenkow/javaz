package org.javaz.easyssl;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.HttpResponse;

import java.io.ByteArrayOutputStream;

/**
 *
 */
public class UrlFetcher
{
    public static void configureSSLHandling(HttpClient client)
    {
        Scheme http = new Scheme("http", 80, PlainSocketFactory.getSocketFactory());
        SSLSocketFactory sf = AllAllowSslSocketFactory.buildSSLSocketFactory();
        Scheme https = new Scheme("https", 443, sf);
        SchemeRegistry sr = client.getConnectionManager().getSchemeRegistry();
        sr.register(http);
        sr.register(https);
    }

    public static byte[] fetchDataFromUrl(String url) throws Exception
    {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        configureSSLHandling(httpClient);

        HttpGet method = new HttpGet(url);
        HttpResponse execute = httpClient.execute(method);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        execute.getEntity().writeTo(bos);
        return bos.toByteArray();
    }
}
