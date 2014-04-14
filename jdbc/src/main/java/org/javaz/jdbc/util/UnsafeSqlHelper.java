package org.javaz.jdbc.util;

import java.sql.*;
import java.util.*;

/**
 *
 */
public class UnsafeSqlHelper implements JdbcConstants
{
    public static ArrayList runSqlUnsafe(ConnectionProviderI provider, String jdbcAddress, String query, int code, Map parameters)
    {
        Connection c = null;
        try
        {
            c = provider.getConnection(jdbcAddress);
            return runSqlUnsafe(c, query, code, parameters);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Problem with query: {" + query + "} code " + code + " and params: " + parameters);
        }
        finally
        {
            if (c != null)
            {
                try
                {
                    c.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static ArrayList runSqlUnsafe(Connection c, String query, int code, Map parameters)
    {
        ArrayList listToReturn = new ArrayList();
        try
        {
            boolean generatedKeysSupportedAndRequired = false;
            PreparedStatement preparedStatement = null;
            if (code == ACTION_EXECUTE_UPDATE)
            {
                generatedKeysSupportedAndRequired = c.getMetaData().supportsGetGeneratedKeys();
                if (generatedKeysSupportedAndRequired)
                {
                    preparedStatement = c.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS);
                }
            }
            if (preparedStatement == null)
            {
                preparedStatement = c.prepareStatement(query);
            }
            if (parameters != null)
            {
                Set set = parameters.keySet();
                for (Iterator iterator = set.iterator(); iterator.hasNext(); )
                {
                    Integer key = (Integer) iterator.next();
                    Object parameterValue = parameters.get(key);
                    if (parameterValue != null && parameterValue instanceof java.util.Date
                            && !(parameterValue instanceof java.sql.Date)
                            && !(parameterValue instanceof java.sql.Time)
                            && !(parameterValue instanceof java.sql.Timestamp))
                    {
                        //we have here old plain java.util.Date.
                        //let's convert it into java.sql.Timestamp and let's see what comes out.
                        java.util.Date d = (java.util.Date) parameterValue;
                        parameterValue = new Timestamp(d.getTime());
                    }
                    if(parameterValue instanceof Object[]) {
                        Object[] arr = (Object[]) parameterValue;
                        Object notNull = null;
                        for (int i = 0; notNull == null && i < arr.length; i++) {
                            notNull = arr[i];
                        }
                        if(notNull != null ) {
                            String type = getSqlType(notNull);
                            Array arrayOf = preparedStatement.getConnection().createArrayOf(type, arr);
                            preparedStatement.setObject(key, arrayOf);
                        } else {
                            //bad choice, but at least.
                            preparedStatement.setObject(key, null);
                        }
                    } else {
                        preparedStatement.setObject(key, parameterValue);
                    }
                }
            }

            if (code == JdbcHelper.ACTION_EXECUTE_UPDATE || code == JdbcHelper.ACTION_EXECUTE_UPDATE_DATA_IGNORE)
            {
                int executeUpdateResult = preparedStatement.executeUpdate();
                if (generatedKeysSupportedAndRequired)
                {
                    ResultSet keys = preparedStatement.getGeneratedKeys();
                    if (keys != null && keys.next())
                    {
                        Object object = keys.getObject(1);
                        listToReturn.add(object);
                    }
                }
                //even in case of GeneratedKeys we return update result itself
                listToReturn.add(new Integer(executeUpdateResult));
                return listToReturn;
            }
            boolean successfulExecution = preparedStatement.execute();
            if (successfulExecution)
            {
                ResultSet resultSet = preparedStatement.getResultSet();
                if (code == JdbcHelper.ACTION_MAP_RESULTS_SET)
                {
                    while (resultSet.next())
                    {
                        HashMap results = new HashMap();
                        ResultSetMetaData setMetaData = resultSet.getMetaData();
                        for (int i = 1; i <= setMetaData.getColumnCount(); i++)
                        {
                            String name = setMetaData.getColumnLabel(i);
                            Object o = null;
                            try
                            {
                                o = resultSet.getObject(i);
                            }
                            catch (SQLException e)
                            {
                                //This can happen with MySQL, let's silent ignore it.
                                //System.out.println("Incorrect object in ResultSet");
                            }
                            if(o instanceof Array) {
                                Array array = (Array) o;
                                o = array.getArray();
                            }
                            if(o instanceof Blob) {
                                Blob blob = (Blob) o;
                                if(blob.length() > 0)
                                    o = blob.getBytes(1/*SQL*/, (int) blob.length());
                            }
                            if(o instanceof Clob) {
                                Clob clob = (Clob) o;
                                if(clob.length() > 0)
                                    o = clob.getSubString(1/*SQL*/, (int) clob.length());
                            }
                            results.put(name.toLowerCase(), o);
                        }

                        listToReturn.add(results);
                    }
                }
                if (code == JdbcHelper.ACTION_LIST_FIRST_OBJECTS)
                {
                    while (resultSet.next())
                    {
                        listToReturn.add(resultSet.getObject(1));
                    }
                }
                if (code == JdbcHelper.ACTION_COMPLEX_LIST_METADATA || code == JdbcHelper.ACTION_COMPLEX_LIST_NO_METADATA)
                {
                    ResultSetMetaData setMetaData = preparedStatement.getMetaData();
                    if (code == JdbcHelper.ACTION_COMPLEX_LIST_METADATA)
                    {
                        ArrayList a = new ArrayList();
                        for (int i = 1; i <= setMetaData.getColumnCount(); i++)
                        {
                            a.add(setMetaData.getColumnLabel(i).toLowerCase());
                        }
                        listToReturn.add(a);
                    }

                    while (resultSet.next())
                    {
                        ArrayList a = new ArrayList();
                        for (int i = 1; i <= setMetaData.getColumnCount(); i++)
                        {
                            a.add(resultSet.getObject(i));
                        }
                        listToReturn.add(a);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("Problem with query: {" + query + "} code " + code + " and params: " + parameters);
        }
        return listToReturn;
    }

    public static String getSqlType(Object notNull) {
        String s = notNull.getClass().getSimpleName().toLowerCase();
        if(System.getProperty("org.javaz.sql.type." + s) != null) {
            return System.getProperty("org.javaz.sql.type." + s);
        }
        return s;
    }

    public static ArrayList runMassSqlUnsafe(ConnectionProviderI provider, String jdbcAddress, ArrayList<Object[]> objects)
    {
        ArrayList<List> ret = new ArrayList<List>();
        Connection c = null;
        try
        {
            c = provider.getConnection(jdbcAddress);
            try
            {
                for (Iterator<Object[]> iteratorX = objects.iterator(); iteratorX.hasNext(); )
                {
                    Object[] next = iteratorX.next();
                    String query = (String) next[0];
                    Map parameters = (Map) next[1];

                    ArrayList list = runSqlUnsafe(c, query, ACTION_EXECUTE_UPDATE, parameters);
                    ret.add(list);
                }
            }
            catch (Exception e)
            {
                //we are allowing partial update;
                e.printStackTrace();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            System.err.println("Problem with connection to " + jdbcAddress);
        }
        finally
        {
            if (c != null)
            {
                try
                {
                    c.close();
                }
                catch (SQLException e)
                {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    public static void addArrayParameters(Map parameters, Collection list)
    {
        setArrayParameter(parameters, parameters.size() + 1, list);
    }

    public static void setArrayParameter(Map parameters, int start, Collection list)
    {
        int i = 0;
        for (Iterator iterator = list.iterator(); iterator.hasNext(); )
        {
            Object x = iterator.next();
            parameters.put(i + start, x);
            i++;
        }
    }

    public static void addArrayParameters(Map parameters, Object[] list)
    {
        setArrayParameter(parameters, parameters.size() + 1, list);
    }

    public static void setArrayParameter(Map parameters, int start, Object[] list)
    {
        for (int i = 0; i < list.length; i++)
        {
            Object x = list[i];
            parameters.put(i + start, x);
        }
    }

    public static String repeatQuestionMark(int size)
    {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < size; i++)
        {
            sb.append(i > 0 ? ",?" : "?");
        }
        return sb.toString();
    }
}
