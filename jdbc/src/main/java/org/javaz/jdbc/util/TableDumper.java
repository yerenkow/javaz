package org.javaz.jdbc.util;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

/**
 * Created by user on 07.08.14.
 * Handy tool to convert DB content into bunch of INSERT's
 */
public class TableDumper {

    public static final int DB_SQL = 0;
    public static final int DB_POSTGRESQL = 1;
    public static final int DB_MYSQL = 2;

    public static String getTableInserts(String tableName, String orderColumn, JdbcHelperI db)
    {
        return getTableInserts(tableName, null, orderColumn, false, db, DB_POSTGRESQL);
    }

    public static String getTableInserts(String tableName, String orderColumn, JdbcHelperI db, int dbType)
    {
        return getTableInserts(tableName, null, orderColumn, false, db, dbType);
    }

    public static String getTableInserts(String tableName, String condition, String orderColumn, boolean skipId,
                                         JdbcHelperI db, int dbType)
    {
        StringBuilder answer = new StringBuilder();
        String query = "select * from " + tableName
                + " WHERE TRUE " + (condition != null ? condition : "")
                + (orderColumn != null ? " order by " + orderColumn : "");

        int idIndex =  0;
        ArrayList complexList = null;
        try {
            complexList = UnsafeSqlHelper.runSqlUnsafe(db.getProvider(), db.getJdbcAddress(), query,
                    UnsafeSqlHelper.ACTION_COMPLEX_LIST_METADATA, null);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if(complexList != null && complexList.size() > 1)
        {
            boolean first = true;
            String firstLine = "";
            String zpt = "";
            for (Iterator iterator = complexList.iterator(); iterator.hasNext();)
            {
                if(first)
                {
                    ArrayList sets = (ArrayList) iterator.next();
                    firstLine += "INSERT INTO " + tableName + "(";
                    String  zpt2 = "";
                    int i = 0;
                    for (Iterator iterator1 = sets.iterator(); iterator1.hasNext();)
                    {
                        String s = (String) iterator1.next();
                        if(s.equalsIgnoreCase("id"))
                        {
                            idIndex = i;
                        }
                        if(idIndex == i && skipId)
                        {
                            i++;
                            continue;
                        }
                        i++;
                        firstLine += zpt2;
                        firstLine += s;
                        zpt2 = ", ";
                    }
                    firstLine += ") VALUES ";
                    first = false;
                    answer.append(firstLine);
                }
                else
                {
                    ArrayList sets = (ArrayList) iterator.next();
                    answer.append(zpt).append("\n");
                    answer.append("(");
                    String  zpt2 = "";
                    int i = 0;
                    for (Iterator iterator1 = sets.iterator(); iterator1.hasNext();)
                    {
                        Object s = (Object) iterator1.next();
                        if(idIndex == i && skipId)
                        {
                            i++;
                            continue;
                        }
                        i++;
                        answer.append(zpt2);
                        if(s != null)
                        {
                            s = ("" + s).replaceAll("\\\\", "\\\\\\\\").replaceAll("\\\'", "''").replaceAll("\\n", "\\\\\\n").replaceAll("\\r", "\\\\\\r");
                            if(dbType == DB_POSTGRESQL) {
                                answer.append("E'");
                            }
                            if(dbType == DB_SQL) {
                                answer.append("'");
                            }
                            if(dbType == DB_MYSQL) {
                                answer.append("\"");
                            }

                            answer.append(s);

                            if(dbType == DB_POSTGRESQL) {
                                answer.append("'");
                            }
                            if(dbType == DB_SQL) {
                                answer.append("'");
                            }
                            if(dbType == DB_MYSQL) {
                                answer.append("\"");
                            }
                        }
                        else
                        {
                            answer.append("NULL");
                        }
                        zpt2 = ", ";
                    }
                    answer.append(")");
                    zpt = ",";
                }
            }
            answer.append(";").append("\n");
        }

        return answer.toString();
    }
}

