package org.javaz.servlet;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.common.XmlRpcHttpRequestConfig;
import org.apache.xmlrpc.server.AbstractReflectiveHandlerMapping;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServlet;
import org.javaz.util.UpdateableAuthPropertyUtil;

/**
 *
 */
public class SecureServlet extends XmlRpcServlet
{
    private String authFile = null;

    public String getAuthFile()
    {
        return authFile;
    }

    public void setAuthFile(String authFile)
    {
        this.authFile = authFile;
    }

    protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException
    {
        PropertyHandlerMapping mapping = (PropertyHandlerMapping) super.newXmlRpcHandlerMapping();
        AbstractReflectiveHandlerMapping.AuthenticationHandler handler =
                new AbstractReflectiveHandlerMapping.AuthenticationHandler()
                {
                    public boolean isAuthorized(XmlRpcRequest pRequest)
                    {
                        XmlRpcHttpRequestConfig config =
                                (XmlRpcHttpRequestConfig) pRequest.getConfig();
                        boolean authenticated = UpdateableAuthPropertyUtil.getInstance(authFile).isAuthorized(config.getBasicUserName(),
                                config.getBasicPassword(), pRequest.getMethodName());
                        return authenticated;
                    }
                };
        mapping.setAuthenticationHandler(handler);
        return mapping;
    }
}

