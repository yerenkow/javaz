package org.javaz.jdbc.replicate;

import org.javaz.jdbc.util.ConnectionProviderI;
import org.javaz.jdbc.util.SimpleConnectionProvider;

import java.sql.*;
import java.util.*;

/**
 * This class make data replication from tables from one DB to other tables in other DB.
 * As parameters, should be specified JSON with such structure:
 * <p>
 * // from - DS address of 1 server
 * // to - DS address of 2 server
 * // type2 - type of DB2, if mysql then column names will be `escaped`
 * <p>
 * // tables: array of:
 * name  - table name in DB1
 * name2 - table name in DB2, optional if name = name2
 * where1 - additional SQL condition ... for table 1, for select, must starts with " AND ..."
 * where2 - additional SQL condition ... for table 2, for delete &amp; select, must starts with " AND ..."
 * <p>
 * {"from":"java:/DBAddress1", "to":"java:/OtherJDBC_DS", "type2":"mysql", "":[{"name":"table_in_db_1", "name2":"table_in_other_db_other_name"}, ...] }
 * <p>
 * It's POC, but it's fully usable on less than hundreds of thousands rows.
 */
public class ReplicateTables
{
    public static String TYPE_MYSQL = "mysql";
    public static String TYPE_POSTGRESQL = "postgresql";
    public static HashMap dbQuotes = new HashMap();

    static
    {
        dbQuotes.put(TYPE_MYSQL, "`");
    }

    public boolean verbose = true;
    public String dbFrom = null;
    public String dbTo = null;
    public String dbToType = TYPE_POSTGRESQL;

    private StringBuffer log = new StringBuffer();
    public ArrayList<HashMap<String, String>> tables = new ArrayList<HashMap<String, String>>();
    public int stampPrecision = 3000;
    public ConnectionProviderI providerI = new SimpleConnectionProvider();

    //todo id specify
    //todo ignore fields
    //todo specified fields
    //todo fields-marks of success replicate

    public String getLog()
    {
        return log.toString();
    }

    public void clearLog()
    {
        log = new StringBuffer();
    }

    public void init(Object parameters)
    {
/*
        Example init from JSON

		dbFrom = null;
		dbTo = null;
		tables.clear();

		JSONObject jsonObject = null;
		try
		{
			jsonObject = JSONObject.fromObject(parameters);
			dbFrom = jsonObject.getString("from");
			dbTo = jsonObject.getString("to");
			if (jsonObject.containsKey("verbose"))
			{
				try
				{
					verbose = Boolean.valueOf(jsonObject.getString("verbose"));
				}
				catch (Exception e)
				{
					verbose = true;
				}
			}
			if (jsonObject.containsKey("type2"))
			{
				dbToType = jsonObject.getString("type2");
			}
			if (jsonObject.containsKey("stampPrecision"))
			{
				stampPrecision = jsonObject.getInt("stampPrecision");
			}
			JSONArray jsonArray = jsonObject.getJSONArray("tables");
			Iterator iterator = jsonArray.iterator();
			while (iterator.hasNext())
			{
				JSONObject table = (JSONObject) iterator.next();
				HashMap tableInfo = new HashMap();
				String name = table.getString("name");
				String name2 = name;
				tableInfo.put("where1", "");
				tableInfo.put("where2", "");
				if (table.containsKey("name2"))
				{
					name2 = table.getString("name2");
				}
				if (table.containsKey("where1"))
				{
					tableInfo.put("where1", table.getString("where1"));
				}
				if (table.containsKey("where2"))
				{
					tableInfo.put("where2", table.getString("where2"));
				}
				tableInfo.put("name", name);
				tableInfo.put("name2", name2);
				tables.add(tableInfo);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			log.append(e.getMessage());
		}
*/
    }

    public boolean equalRecords(Object a, Object b)
    {
        if (a instanceof java.sql.Timestamp && b instanceof java.sql.Timestamp)
        {
            //hack here, since different DB realization;
            long diff = ((Timestamp) a).getTime() - ((Timestamp) b).getTime();
            //if more than N milliseconds;
            if (Math.abs(diff) < stampPrecision)
            {
                return true;
            }
        }

        return a.equals(b);
    }

    public void runReplicate() throws InterruptedException
    {
        if (verbose)
            log.append("Started at " + Calendar.getInstance().getTime() + "\r\n");
        Connection connectionFrom = null;
        Connection connectionTo = null;
        try
        {
            connectionFrom = providerI.getConnection(dbFrom);
            connectionTo = providerI.getConnection(dbTo);
            PreparedStatement preparedStatementFrom = null;
            PreparedStatement preparedStatementTo = null;
            ResultSet resultSet = null;

            for (Iterator<HashMap<String, String>> HashMapIterator = tables.iterator(); HashMapIterator.hasNext(); )
            {
                try
                {
                    HashMap<String, String> h = HashMapIterator.next();
                    String name = h.get("name");
                    String name2 = h.get("name2");
                    String where1 = h.get("where1");
                    String where2 = h.get("where2");

                    if (verbose)
                        log.append("Replicating " + dbFrom + "/" + name + " to " + dbTo + "/" + name2 + "\r\n");

                    preparedStatementFrom = connectionFrom.prepareStatement("select * from " + name + " WHERE 1=1 " + where1);
                    boolean b = preparedStatementFrom.execute();
                    HashMap meta = new HashMap();
                    //1. find all ids from DB1
                    ArrayList db1 = new ArrayList();
                    String nullMark = "____NULL";
                    if (b)
                    {
                        resultSet = preparedStatementFrom.getResultSet();
                        ResultSetMetaData setMetaData = resultSet.getMetaData();
                        for (int i = 1; i <= setMetaData.getColumnCount(); i++)
                        {
                            meta.put(setMetaData.getColumnLabel(i).toLowerCase(), setMetaData.getColumnClassName(i));
                        }
                        while (resultSet.next())
                        {
                            HashMap results = new HashMap();
                            for (int i = 1; i <= setMetaData.getColumnCount(); i++)
                            {
                                String columnName = setMetaData.getColumnLabel(i).toLowerCase();
                                Object o = resultSet.getObject(i);
                                if (o != null)
                                {
                                    results.put(columnName, o);
                                }
                                else
                                {
                                    results.put(columnName + nullMark, nullMark);
                                }
                            }
                            db1.add(results);
                        }
                        resultSet.close();
                    }
                    else
                    {
                        log.append("Couldn't execute select from " + dbFrom + "/" + name + " /r/n");
                        return;
                    }

                    StringBuffer allIds = new StringBuffer();
                    HashMap records1 = new HashMap();
                    HashMap toInsert = new HashMap();
                    HashMap toUpdate = new HashMap();
                    for (Iterator iterator = db1.iterator(); iterator.hasNext(); )
                    {
                        HashMap record = (HashMap) iterator.next();
                        if (allIds.length() != 0)
                        {
                            allIds.append(",");
                        }
                        allIds.append(record.get("id"));
                        records1.put(record.get("id"), record);
                    }

                    toInsert.putAll(records1);

                    //2. find all ids to delete in DB2;
                    if (allIds.length() > 0)
                    {
                        preparedStatementTo = connectionTo.prepareStatement("delete from " + name2 + " where id not in (" + allIds.toString() + ")" + where2);
                        if (verbose)
                            log.append("deleted from " + dbTo + "/" + name2 + " " + preparedStatementTo.executeUpdate() + " records;\r\n");
                    }
                    else
                    {
                        if (verbose)
                            log.append("No records in " + dbFrom + "/" + name + ", nothing to delete in " + dbTo + "/" + name2 + " ;\r\n");
                    }

                    //3. find all ids from DB2;
                    preparedStatementTo = connectionTo.prepareStatement("select * from " + name2 + " WHERE 1=1 " + where2);
                    b = preparedStatementTo.execute();
                    HashMap meta2 = new HashMap();
                    ArrayList db2 = new ArrayList();
                    if (b)
                    {
                        resultSet = preparedStatementTo.getResultSet();
                        ResultSetMetaData setMetaData = resultSet.getMetaData();
                        for (int i = 1; i <= setMetaData.getColumnCount(); i++)
                        {
                            meta2.put(setMetaData.getColumnLabel(i).toLowerCase(), setMetaData.getColumnClassName(i));
                        }
                        while (resultSet.next())
                        {
                            HashMap results = new HashMap();
                            for (int i = 1; i <= setMetaData.getColumnCount(); i++)
                            {
                                String columnName = setMetaData.getColumnLabel(i).toLowerCase();
                                Object o = resultSet.getObject(i);
                                if (o != null)
                                {
                                    results.put(columnName, o);
                                }
                                else
                                {
                                    results.put(columnName + nullMark, nullMark);
                                }
                            }
                            db2.add(results);
                        }
                    }
                    else
                    {
                        log.append("Couldn't execute select from " + dbTo + "/" + name2 + " /r/n");
                        return;
                    }

                    //compare meta-data;
                    {
                        HashMap temp = new HashMap();
                        {
                            temp.putAll(meta);

                            Set set = meta2.keySet();
                            for (Iterator iteratorSet = set.iterator(); iteratorSet.hasNext(); )
                            {
                                Object o = iteratorSet.next();
                                if (meta.containsKey(o))
                                {
                                    if (meta.get(o).equals(meta2.get(o)))
                                    {
                                        temp.remove(o);
                                    }
                                }
                                else
                                {
                                    log.append("ERROR: Meta data not equals! \r\n");
                                    log.append(o + "\t" + temp.get(o) + " not present in table " + name + "\r\n");
                                }
                            }
                        }
                        if (!temp.isEmpty())
                        {
                            log.append("ERROR: Meta data not equals! \r\n");
                            Set set = temp.keySet();
                            for (Iterator iteratorSet = set.iterator(); iteratorSet.hasNext(); )
                            {
                                Object o = iteratorSet.next();
                                log.append(o + "\t" + temp.get(o) + " != " + meta2.get(o) + "\r\n");
                            }

                            return;
                        }
                    }

                    for (Iterator iterator = db2.iterator(); iterator.hasNext(); )
                    {
                        HashMap db2Record = (HashMap) iterator.next();
                        if (toInsert.containsKey(db2Record.get("id")))
                        {
                            HashMap db1Record = (HashMap) toInsert.get(db2Record.get("id"));
                            boolean equal = true;
                            Set set = meta2.keySet();
                            for (Iterator iteratorSet = set.iterator(); equal && iteratorSet.hasNext(); )
                            {
                                String columnName2 = (String) iteratorSet.next();
                                if (db2Record.containsKey(columnName2 + nullMark) ||
                                        db1Record.containsKey(columnName2 + nullMark))
                                {
                                    equal = db2Record.containsKey(columnName2 + nullMark) && db1Record.containsKey(columnName2 + nullMark);
                                }
                                else
                                {
                                    //checking not-null;
                                    equal = equalRecords(db1Record.get(columnName2), db2Record.get(columnName2));
                                }
                            }
                            if (!equal)
                            {
                                toUpdate.put(db2Record.get("id"), toInsert.get(db2Record.get("id")));
                            }
                            toInsert.remove(db2Record.get("id"));
                        }
                        else
                        {
                            //this case shouldn't happen at all, since we've deleted all such records

                        }
                    }

                    log.append("Found " + toUpdate.size() + " to update, and " + toInsert.size() + " to insert.\r\n");
                    int totalUpdated = 0;
                    //4. calculate all to update in DB2
                    if (!toUpdate.isEmpty())
                    {
                        Set set = toUpdate.keySet();
                        for (Iterator iteratorSet = set.iterator(); iteratorSet.hasNext(); )
                        {
                            StringBuffer sql = new StringBuffer();
                            Object id = iteratorSet.next();
                            HashMap r = (HashMap) toUpdate.get(id);
                            sql.append("UPDATE " + name2 + " SET ");
                            StringBuffer values = new StringBuffer();

                            Set en = meta2.keySet();
                            for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); )
                            {
                                Object o = iteratorSetEn.next();
                                if (!o.equals("id"))
                                {
                                    if (values.length() != 0)
                                    {
                                        values.append(",");
                                    }
                                    Object quote = dbQuotes.get(dbToType);
                                    if (quote == null)
                                    {
                                        quote = "";
                                    }
                                    values.append(quote).append(o).append(quote);
                                    values.append(" =  ? ");
                                }
                            }
                            values.append(" WHERE id = '" + r.get("id") + "';");
                            PreparedStatement statement = connectionTo.prepareStatement(sql.toString() + values.toString());
                            en = meta2.keySet();
                            int i = 0;
                            for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); )
                            {
                                Object o = iteratorSetEn.next();
                                if (!o.equals("id"))
                                {
                                    i++;
                                    statement.setObject(i, r.get(o));
                                }
                            }
                            try
                            {
                                totalUpdated += statement.executeUpdate();
                            }
                            catch (SQLException e)
                            {
                                e.printStackTrace();
                                log.append("Error occured: " + e + "\r\n");
                            }
                        }
                    }
                    if (verbose)
                        log.append("Updated " + totalUpdated + " records.\r\n");

                    //4. calculate all to insert to DB2
                    if (!toInsert.isEmpty())
                    {
                        StringBuffer header = new StringBuffer();
                        if (header.length() == 0)
                        {
                            header.append(" INSERT INTO " + name2 + " (");
                            StringBuffer columns = new StringBuffer();
                            Set en = meta2.keySet();
                            for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); )
                            {
                                Object o = iteratorSetEn.next();
                                if (columns.length() != 0)
                                {
                                    columns.append(",");
                                }

                                Object quote = dbQuotes.get(dbToType);
                                if (quote == null)
                                {
                                    quote = "";
                                }
                                columns.append(quote).append(o).append(quote);
                            }
                            header.append(columns.toString());
                            header.append(") VALUES ");
                        }

                        Set enumeration = toInsert.keySet();
                        for (Iterator iteratorSetX = enumeration.iterator(); iteratorSetX.hasNext(); )
                        {
                            Object id = iteratorSetX.next();
                            HashMap r = (HashMap) toInsert.get(id);
                            StringBuffer values = new StringBuffer();
                            if (values.length() != 0)
                            {
                                values.append(",");
                            }
                            values.append("(");

                            StringBuffer columns = new StringBuffer();
                            Set en = meta2.keySet();
                            for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); )
                            {
                                Object o = iteratorSetEn.next();
                                if (columns.length() != 0)
                                {
                                    columns.append(",");
                                }
                                columns.append(" ? ");
                            }
                            values.append(columns.toString());
                            values.append(");");

                            PreparedStatement statement = connectionTo.prepareStatement(header.toString() + values.toString());
                            en = meta2.keySet();
                            int i = 0;
                            for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); )
                            {
                                Object o = iteratorSetEn.next();
                                i++;
                                statement.setObject(i, r.get(o));
                            }
                            statement.execute();
                        }
                    }

                    if (verbose)
                        log.append("Replication finished OK.\r\n");
                }
                catch (Exception e)
                {
                    log.append("Some error occured: " + e + "\r\n");
                    log.append(e.getMessage() + "\r\n");
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            log.append("Error with query = " + e);
            log.append(e.getMessage());
            return;
        }
        finally
        {
            try
            {
                if (connectionFrom != null && !connectionFrom.isClosed())
                {
                    connectionFrom.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
            try
            {
                if (connectionTo != null && !connectionTo.isClosed())
                {
                    connectionTo.close();
                }
            }
            catch (SQLException e)
            {
                e.printStackTrace();
            }
        }
        if (verbose)
            log.append("Ended at " + Calendar.getInstance().getTime());
    }
}
