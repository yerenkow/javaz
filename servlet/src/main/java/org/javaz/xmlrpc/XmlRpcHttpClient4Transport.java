package org.javaz.xmlrpc;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcHttpClientConfig;
import org.apache.xmlrpc.client.XmlRpcHttpTransport;
import org.apache.xmlrpc.common.XmlRpcStreamRequestConfig;
import org.apache.xmlrpc.util.HttpUtil;
import org.apache.xmlrpc.util.XmlRpcIOException;
import org.javaz.easyssl.UrlFetcher;
import org.xml.sax.SAXException;

public class XmlRpcHttpClient4Transport extends XmlRpcHttpTransport
{

    private static final int MAX_REDIRECT_ATTEMPTS = 10;
    private HttpClient client;
    private static final String userAgent = USER_AGENT + " (HC 4)";
    private HttpPost method;
    private int contentLength = -1;
    private HttpResponse httpResponse;

    public XmlRpcHttpClient4Transport(XmlRpcHttpClient4TransportFactory pFactory)
    {
        super(pFactory.getClient(), userAgent);
        client = pFactory.getHttpClient();
        if (client == null)
        {
            client = new DefaultHttpClient();
        }
    }

    @Override
    protected void setContentLength(int pLength)
    {
        contentLength = pLength;
    }

    @Override
    protected void initHttpHeaders(XmlRpcRequest pRequest) throws XmlRpcClientException
    {

        XmlRpcHttpClientConfig config = (XmlRpcHttpClientConfig) pRequest.getConfig();
        try
        {
            method = new HttpPost(config.getServerURL().toURI());
        }
        catch (URISyntaxException ex)
        {
            throw new XmlRpcClientException("", ex);
        }
        super.initHttpHeaders(pRequest);
    }

    @Override
    protected void setRequestHeader(String pHeader, String pValue)
    {
        method.setHeader(pHeader, pValue);
    }

    @Override
    protected void close() throws XmlRpcClientException
    {
        method.releaseConnection();
    }

    @Override
    protected boolean isResponseGzipCompressed(XmlRpcStreamRequestConfig pConfig)
    {
        Header[] headers = method.getHeaders("Content-Encoding");
        if (headers == null || headers.length < 1)
        {
            return false;
        }
        else
        {
            Header h = headers[0];
            return HttpUtil.isUsingGzipEncoding(h.getValue());
        }
    }

    @Override
    protected InputStream getInputStream() throws XmlRpcException
    {
        try
        {
            int statusCode = httpResponse.getStatusLine().getStatusCode();
            // All status codes except SC_OK are handled as errors. Perhaps some should require special handling (e.g., SC_UNAUTHORIZED)
            if (statusCode < 200 || statusCode > 299)
            {
                throw new XmlRpcClientException("statusCode : " + statusCode, new Throwable(httpResponse.getStatusLine().getReasonPhrase()));
            }
            return httpResponse.getEntity().getContent();
        }
        catch (ClientProtocolException ex)
        {
            throw new XmlRpcClientException("", ex);
        }
        catch (IOException ex)
        {
            throw new XmlRpcClientException("", ex);
        }
    }

    @Override
    protected void writeRequest(final ReqWriter pWriter) throws XmlRpcException, IOException, SAXException
    {
        BasicHttpEntity entity = new BasicHttpEntity();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pWriter.write(baos);

        BufferedInputStream bis = new BufferedInputStream(new ByteArrayInputStream(baos.toByteArray()));
        entity.setContent(bis);
        entity.setContentLength(contentLength);
        entity.setContentType("text/xml");
        method.setEntity(entity);
        try
        {
            int redirectAttempts = 0;
            for (; ; )
            {
                httpResponse = client.execute(method);
                if (!isRedirectRequired())
                {
                    break;
                }
                if (redirectAttempts++ > MAX_REDIRECT_ATTEMPTS)
                {
                    throw new XmlRpcException("Too many redirects.");
                }
                resetClientForRedirect();
            }
        }
        catch (XmlRpcIOException e)
        {
            Throwable t = e.getLinkedException();
            if (t instanceof XmlRpcException)
            {
                throw (XmlRpcException) t;
            }
            else
            {
                throw new XmlRpcException("Unexpected exception: " + t.getMessage(), t);
            }
        }
        catch (IOException e)
        {
            throw new XmlRpcException("I/O error while communicating with HTTP server: " + e.getMessage(), e);
        }
    }

    protected boolean isRedirectRequired()
    {
        switch (httpResponse.getStatusLine().getStatusCode())
        {
            case HttpStatus.SC_MOVED_TEMPORARILY:
            case HttpStatus.SC_MOVED_PERMANENTLY:
            case HttpStatus.SC_SEE_OTHER:
            case HttpStatus.SC_TEMPORARY_REDIRECT:
                return true;
            default:
                return false;
        }
    }

    protected void resetClientForRedirect() throws XmlRpcException
    {
        //get the location header to find out where to redirect to
        Header locationHeader = httpResponse.getFirstHeader("location");
        if (locationHeader == null)
        {
            throw new XmlRpcException("Invalid redirect: Missing location header");
        }
        String location = locationHeader.getValue();

        URI redirectUri = null;
        try
        {
            redirectUri = new URI(location);
            method.setURI(redirectUri);
        }
        catch (URISyntaxException ex)
        {
            throw new XmlRpcException("", ex);
        }
    }
}
