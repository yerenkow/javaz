package org.javaz.jdbc.util;

/**
 * This factory creates ConnectionProviders by dbAddress.
 * You should extend this factory and implement own ConnectionProviderI
 * If you need use pools or do something else
 */
public class ConnectionProviderFactory
{
    public static final String PROVIDER_BONECP = "BoneCpConnectionProvider";
    public static final String PROVIDER_SIMPLE = "SimpleConnectionProvider";

    private String connectionProviderClass = PROVIDER_BONECP;

    public ConnectionProviderFactory() {
    }

    public ConnectionProviderFactory(String connectionProviderClass) {
        this.connectionProviderClass = connectionProviderClass;
    }

    public String getConnectionProviderClass() {
        return connectionProviderClass;
    }

    public void setConnectionProviderClass(String connectionProviderClass) {
        this.connectionProviderClass = connectionProviderClass;
    }

    public ConnectionProviderI createProvider(String dbAddress)
    {
        if (connectionProviderClass.equals(PROVIDER_SIMPLE)) {
            return new SimpleConnectionProvider();
        }

        if (connectionProviderClass.equals(PROVIDER_BONECP)) {
            return new BoneCpConnectionProvider();
        }

        try {
            return (ConnectionProviderI) (Class.forName(connectionProviderClass).newInstance());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

}
