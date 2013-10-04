package org.javaz.jdbc.util;

/**
 * This factory creates ConnectionProviders by dbAddress.
 * You should extend this factory and implement own ConnectionProviderI
 * If you need use pools or do something else
 */
public class ConnectionProviderFactory
{
    public static ConnectionProviderFactory instance = new ConnectionProviderFactory();

    public ConnectionProviderI createProvider(String dbAddress)
    {
        return new SimpleConnectionProvider();
    }

}
