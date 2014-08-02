package org.javaz.jdbc.util;

/**
 *
 */
public interface JdbcConstants
{
    /**
     * Marker, so ConnectionProviderI can guess that it's JDBC URL
     */
    public static final String JDBC_MARKER = "jdbc:";

    /**
     * Marker, so ConnectionProviderI can guess that it's JNDI NAME
     */
    public static final String JAVA_MARKER = "java:";

    /**
     * This is code to make update queries, and tries to return any generated keys
     * if DB supported it
     * <p>
     * List (key1, key2, ... result)
     */
    public static final int ACTION_EXECUTE_UPDATE = 0;

    /**
     * Run select, and return list of Maps
     * <p>
     * List (Map, Map ....)
     */
    public static final int ACTION_MAP_RESULTS_SET = 1;

    /**
     * Return list, but not with Maps, but with first objects;
     * <p>
     * List (Object, Object ....)
     */
    public static final int ACTION_LIST_FIRST_OBJECTS = 2;

    /**
     * First object will be List will contain metadata Strings - names of columns
     * Second and all other will be lists with data
     * <p>
     * List (List(c0,c1,c2 ...), List (Object, Object, ...) ....)
     */
    public static final int ACTION_COMPLEX_LIST_METADATA = 3;

    /**
     * All objects will be lists with data
     * <p>
     * List (List (Object, Object, ...), List (Object, Object, ...) ....)
     */
    public static final int ACTION_COMPLEX_LIST_NO_METADATA = 4;

    /**
     * Run update and not try to get generated keys.
     * <p>
     * List (result)
     */
    public static final int ACTION_EXECUTE_UPDATE_DATA_IGNORE = 5;

    public static final long DEFAULT_TTL_LISTS = 300000l;

}
