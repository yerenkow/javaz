package org.javaz.easyssl;

import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 *
 */
public class AllAllowSslSocketFactory
{
    public static SSLSocketFactory buildSSLSocketFactory()
    {
        TrustStrategy ts = new TrustStrategy()
        {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException
            {
                return true; // heck yea!
            }
        };

        SSLSocketFactory sf = null;

        try
        {
            /* build socket factory with hostname verification turned off. */
            sf = new SSLSocketFactory(ts, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        }
        catch (Exception e)
        {
            System.err.println("Failed to initialize SSL handling." + e);
        }

        return sf;
    }
}
