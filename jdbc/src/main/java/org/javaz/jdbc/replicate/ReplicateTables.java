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
public class ReplicateTables {
    public static String TYPE_MYSQL = "mysql";
    public static String TYPE_POSTGRESQL = "postgresql";
    public static String NULL_MARK = "____NULL";
    public static HashMap dbQuotes = new HashMap();

    static {
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

    //todo ignore fields
    //todo specified fields
    //todo fields-marks of success replicate

    public String getLog() {
        return log.toString();
    }

    public void clearLog() {
        log = new StringBuffer();
    }

    public void init(Object parameters) {
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

    public boolean equalRecords(Object a, Object b) {
        if (a instanceof java.sql.Timestamp && b instanceof java.sql.Timestamp) {
            //hack here, since different DB realization;
            long diff = ((Timestamp) a).getTime() - ((Timestamp) b).getTime();
            //if more than N milliseconds;
            if (Math.abs(diff) < stampPrecision) {
                return true;
            }
        }

        return a.equals(b);
    }

    public void runReplicate() throws InterruptedException {
        if (verbose)
            log.append("Started at " + Calendar.getInstance().getTime() + "\r\n");
        Connection connectionFrom = null;
        Connection connectionTo = null;
        try {
            connectionFrom = providerI.getConnection(dbFrom);
            connectionTo = providerI.getConnection(dbTo);

            for (Iterator<HashMap<String, String>> HashMapIterator = tables.iterator(); HashMapIterator.hasNext(); ) {
                HashMap<String, String> h = HashMapIterator.next();
                proceedTable(h, connectionFrom, connectionTo);
            }
        } catch (Exception e) {
            log.append("Error with query = " + e);
            log.append(e.getMessage());
            return;
        } finally {
            try {
                if (connectionFrom != null && !connectionFrom.isClosed()) {
                    connectionFrom.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (connectionTo != null && !connectionTo.isClosed()) {
                    connectionTo.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (verbose)
            log.append("Ended at " + Calendar.getInstance().getTime());
    }

    protected void proceedTable(Map<String, String> h, Connection connectionFrom, Connection connectionTo) {
        try {
            String name1 = h.get("name");
            String name2 = h.get("name2");
            String where1 = h.get("where1");
            String where2 = h.get("where2");
            String pk = h.get("id");
            if (pk == null) {
                pk = "id";
            }
            if (verbose)
                log.append("Replicating " + dbFrom + "/" + name1 + " to " + dbTo + "/" + name2 + "\r\n");
            HashMap meta1 = new HashMap();
            ArrayList db1 = new ArrayList();
            getTableResults(connectionFrom, name1, where1, meta1, db1);

            StringBuffer allIds = new StringBuffer();
            HashMap toInsert = new HashMap();
            for (Iterator iterator = db1.iterator(); iterator.hasNext(); ) {
                HashMap record = (HashMap) iterator.next();
                if (allIds.length() != 0) {
                    allIds.append(",");
                }
                String pkIdExtracted = extractId(record, pk);
                allIds.append(pkIdExtracted);
                toInsert.put(pkIdExtracted, record);
            }

            //2. find all ids to delete in DB2;
            deleteRecords(connectionTo, name1, name2, where2, pk, allIds);
            HashMap meta2 = new HashMap();
            ArrayList db2 = new ArrayList();
            getTableResults(connectionTo, name2, where2, meta2, db2);
            compareMetaData(name1, meta1, meta2);
            HashMap toUpdate = new HashMap();
            findToUpdateRecords(pk, toInsert, toUpdate, meta2, db2);
            log.append("Found " + toUpdate.size() + " to update, and " + toInsert.size() + " to insert.\r\n");
            updateRecords(connectionTo, name2, pk, toUpdate, meta2);
            insertRecords(connectionTo, name2, toInsert, meta2);

            if (verbose)
                log.append("Replication finished OK.\r\n");
        } catch (Exception e) {
            log.append("Some error occured: " + e + "\r\n");
            log.append(e.getMessage() + "\r\n");
            e.printStackTrace();
        }

    }

    private void deleteRecords(Connection connectionTo, String name1, String name2, String where2, String pk,
                               StringBuffer allIds) throws SQLException {
        PreparedStatement preparedStatementTo;
        if (allIds.length() > 0) {
            String deleteSql = "delete from " + name2 + " where " + pkExpression(pk, dbToType) + " not in (" + allIds.toString() + ")" + where2;
            preparedStatementTo = connectionTo.prepareStatement(deleteSql);
            int updateCount = preparedStatementTo.executeUpdate();
            if (verbose)
                log.append("deleted from " + dbTo + "/" + name2 + " " + updateCount + " records;\r\n");
        } else {
            if (verbose)
                log.append("No records in " + dbFrom + "/" + name1 + ", nothing to delete in " + dbTo + "/" + name2 + " ;\r\n");
        }
    }

    private void insertRecords(Connection connectionTo, String name2, HashMap toInsert, HashMap meta2) throws SQLException {
        //4. calculate all to insert to DB2
        if (!toInsert.isEmpty()) {
            StringBuffer header = new StringBuffer();
            if (header.length() == 0) {
                header.append(" INSERT INTO " + name2 + " (");
                StringBuffer columns = new StringBuffer();
                Set en = meta2.keySet();
                for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); ) {
                    Object o = iteratorSetEn.next();
                    if (columns.length() != 0) {
                        columns.append(",");
                    }

                    Object quote = dbQuotes.get(dbToType);
                    if (quote == null) {
                        quote = "";
                    }
                    columns.append(quote).append(o).append(quote);
                }
                header.append(columns.toString());
                header.append(") VALUES ");
            }

            Set enumeration = toInsert.keySet();
            for (Iterator iteratorSetX = enumeration.iterator(); iteratorSetX.hasNext(); ) {
                Object id = iteratorSetX.next();
                HashMap r = (HashMap) toInsert.get(id);
                StringBuffer values = new StringBuffer();
                if (values.length() != 0) {
                    values.append(",");
                }
                values.append("(");

                StringBuffer columns = new StringBuffer();
                Set en = meta2.keySet();
                for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); ) {
                    Object o = iteratorSetEn.next();
                    if (columns.length() != 0) {
                        columns.append(",");
                    }
                    columns.append(" ? ");
                }
                values.append(columns.toString());
                values.append(");");

                PreparedStatement statement = connectionTo.prepareStatement(header.toString() + values.toString());
                en = meta2.keySet();
                int i = 0;
                for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); ) {
                    Object o = iteratorSetEn.next();
                    i++;
                    statement.setObject(i, r.get(o));
                }
                statement.execute();
            }
        }
    }

    private void updateRecords(Connection connectionTo, String name2, String pk, HashMap toUpdate, HashMap meta2) throws SQLException {
        int totalUpdated = 0;
        ArrayList pks = splitPk(pk);
        //4. calculate all to update in DB2
        if (!toUpdate.isEmpty()) {
            Set set = toUpdate.keySet();
            for (Iterator iteratorSet = set.iterator(); iteratorSet.hasNext(); ) {
                StringBuffer sql = new StringBuffer();
                Object id = iteratorSet.next();
                HashMap r = (HashMap) toUpdate.get(id);
                sql.append("UPDATE " + name2 + " SET ");
                StringBuffer values = new StringBuffer();

                Set en = meta2.keySet();
                for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); ) {
                    Object columnName = iteratorSetEn.next();
                    if (!pks.contains(columnName)) {
                        if (values.length() != 0) {
                            values.append(",");
                        }
                        Object quote = dbQuotes.get(dbToType);
                        if (quote == null) {
                            quote = "";
                        }
                        values.append(quote).append(columnName).append(quote);
                        values.append(" =  ? ");
                    }
                }
                values.append(" WHERE " + pkExpression(pk, dbToType) + " = ?");
                PreparedStatement statement = connectionTo.prepareStatement(sql.toString() + values.toString());
                en = meta2.keySet();
                int i = 0;
                for (Iterator iteratorSetEn = en.iterator(); iteratorSetEn.hasNext(); ) {
                    Object columnName = iteratorSetEn.next();
                    if (!pks.contains(columnName)) {
                        i++;
                        statement.setObject(i, r.get(columnName));
                    }
                }
                statement.setObject(++i, extractId(r, pk));
                try {
                    totalUpdated += statement.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                    log.append("Error occured: " + e + "\r\n");
                }
            }
        }
        if (verbose)
            log.append("Updated " + totalUpdated + " records.\r\n");
    }

    private ArrayList splitPk(String pk) {
        ArrayList list = new ArrayList();
        if (pk != null) {
            String[] split = pk.split(",");
            for (int i = 0; i < split.length; i++) {
                String s = split[i];
                list.add(s);
            }
        }
        return list;
    }

    private void findToUpdateRecords(String pk, HashMap toInsert, HashMap toUpdate, HashMap meta2, ArrayList db2) {
        for (Iterator iterator = db2.iterator(); iterator.hasNext(); ) {
            HashMap db2Record = (HashMap) iterator.next();
            String pkIdExpression = extractId(db2Record, pk);
            if (toInsert.containsKey(pkIdExpression)) {
                HashMap db1Record = (HashMap) toInsert.get(pkIdExpression);
                boolean equal = true;
                Set set = meta2.keySet();
                for (Iterator iteratorSet = set.iterator(); equal && iteratorSet.hasNext(); ) {
                    String columnName2 = (String) iteratorSet.next();
                    if (db2Record.containsKey(columnName2 + NULL_MARK) ||
                            db1Record.containsKey(columnName2 + NULL_MARK)) {
                        equal = db2Record.containsKey(columnName2 + NULL_MARK) && db1Record.containsKey(columnName2 + NULL_MARK);
                    } else {
                        //checking not-null;
                        equal = equalRecords(db1Record.get(columnName2), db2Record.get(columnName2));
                    }
                }
                if (!equal) {
                    toUpdate.put(pkIdExpression, toInsert.get(pkIdExpression));
                }
                toInsert.remove(pkIdExpression);
            } else {
                //this case shouldn't happen at all, since we've deleted all such records
            }
        }
    }

    private void compareMetaData(String name, HashMap meta, HashMap meta2) {
        HashMap temp = new HashMap();
        {
            temp.putAll(meta);

            Set set = meta2.keySet();
            for (Iterator iteratorSet = set.iterator(); iteratorSet.hasNext(); ) {
                Object o = iteratorSet.next();
                if (meta.containsKey(o)) {
                    if (meta.get(o).equals(meta2.get(o))) {
                        temp.remove(o);
                    }
                } else {
                    log.append("ERROR: Meta data not equals! \r\n");
                    log.append(o + "\t" + temp.get(o) + " not present in table " + name + "\r\n");
                }
            }
        }
        if (!temp.isEmpty()) {
            log.append("ERROR: Meta data not equals! \r\n");
            Set set = temp.keySet();
            for (Iterator iteratorSet = set.iterator(); iteratorSet.hasNext(); ) {
                Object o = iteratorSet.next();
                log.append(o + "\t" + temp.get(o) + " != " + meta2.get(o) + "\r\n");
            }
        }
    }

    private void getTableResults(Connection connectionFrom, String tableName, String where, HashMap meta, ArrayList db1) throws SQLException {
        PreparedStatement preparedStatementFrom = null;
        ResultSet resultSet = null;
        try {
            preparedStatementFrom = connectionFrom.prepareStatement("select * from " + tableName + " WHERE 1=1 " + where);
            boolean b = preparedStatementFrom.execute();
            if (b) {
                resultSet = preparedStatementFrom.getResultSet();
                ResultSetMetaData setMetaData = resultSet.getMetaData();
                for (int i = 1; i <= setMetaData.getColumnCount(); i++) {
                    meta.put(setMetaData.getColumnLabel(i).toLowerCase(), setMetaData.getColumnClassName(i));
                }
                while (resultSet.next()) {
                    HashMap results = new HashMap();
                    for (int i = 1; i <= setMetaData.getColumnCount(); i++) {
                        String columnName = setMetaData.getColumnLabel(i).toLowerCase();
                        Object o = resultSet.getObject(i);
                        if (o != null) {
                            results.put(columnName, o);
                        } else {
                            results.put(columnName + NULL_MARK, NULL_MARK);
                        }
                    }
                    db1.add(results);
                }
            } else {
                log.append("Couldn't execute select from " + dbFrom + "/" + tableName + " /r/n");
            }
        } finally {
            try {
                if (resultSet != null && !resultSet.isClosed()) {
                    resultSet.close();
                }
            } catch (SQLException e) {
                log.append(e.getMessage());
            }
            try {
                if (preparedStatementFrom != null && !preparedStatementFrom.isClosed()) {
                    preparedStatementFrom.close();
                }
            } catch (SQLException e) {
                log.append(e.getMessage());
            }
        }
    }

    private String pkExpression(String pk, String dbToType) {
        if (pk.contains(",")) {
            return " concat_ws('_', " + pk + ") ";
        } else {
            return " " + pk + " ";
        }
    }

    private String extractId(HashMap record, String pk) {
        String result = "";
        if (pk != null) {
            String[] split = pk.split(",");
            if (split.length > 1) {
                result += "'";
            }
            for (int i = 0; i < split.length; i++) {
                if (i > 0) {
                    result += "_";
                }
                String pkName = split[i];
                Object o = record.get(pkName);
                if (o instanceof Timestamp) {
                    String ts = "" + o;
                    if (ts.contains(".")) {
                        result += ts.substring(0, ts.lastIndexOf("."));
                    } else {
                        result += ts;
                    }
                } else {
                    result += o;
                }
            }
            if (split.length > 1) {
                result += "'";
            }
            return result;
        }
        return null;
    }
}
