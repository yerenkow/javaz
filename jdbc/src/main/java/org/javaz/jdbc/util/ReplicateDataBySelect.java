package org.javaz.jdbc.util;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.javaz.jdbc.ddl.MysqlDdlGenerator;

/**
 */
public class ReplicateDataBySelect {

    private String schema;

    public static String prefix1 = "trepl1_";
    public static String prefix2 = "trepl2_";
    private static String FROM_MARKER = " from ";
    private static String WHERE_MARKER = " where ";
    private static String ORDER_MARKER = " order ";

    private JdbcHelperI helper;
    private boolean mysql = true;

    public ReplicateDataBySelect(String schema, String dbUrl) {
        this.schema = schema;
        helper = JdbcCachedHelper.getInstance(dbUrl);
    }

    public void processSelect(String select) {
        String lowered = select.toLowerCase();
        int fromIndex = lowered.indexOf(FROM_MARKER);
        if (fromIndex == -1) {
            // select not from, ignore it.
            return;
        }
        String fromAndWhere = select.substring(fromIndex + FROM_MARKER.length());

        ArrayList<Aliased> aliaseds = parseJoinedTables(detectFrom(fromAndWhere));
        for (Aliased aliased : aliaseds) {
            copyData(aliased, fromAndWhere, helper);
        }
    }

    private String detectFrom(String fromAndWhere) {
        String lowered = fromAndWhere.toLowerCase();
        int index = lowered.indexOf(WHERE_MARKER);
        if (index != -1) {
            fromAndWhere = fromAndWhere.substring(0, index).trim();
        }
        lowered = fromAndWhere.toLowerCase();
        index = lowered.indexOf(ORDER_MARKER);
        if (index != -1) {
            fromAndWhere = fromAndWhere.substring(0, index).trim();
        }

        return fromAndWhere;
    }

    private void copyData(Aliased aliased, String fromAndWhere, JdbcHelperI instance) {
        if (aliased.getName().isEmpty()) {
            System.out.println(aliased);
            return;
        }
        String tableName1 = prefix1 + aliased.getName();
        String tableName2 = prefix2 + aliased.getName();

        instance.runUpdateDataIgnore("create table if not exists " + tableName1
                + " like " + aliased.getName(), null);
        instance.runUpdateDataIgnore("create table if not exists " + tableName2
                + " like " + aliased.getName(), null);
        // purge always, just in case.
        instance.runUpdateDataIgnore("delete from " + tableName1, null);
        instance.runUpdateDataIgnore("insert into " + tableName1 + " select "
                + aliased.getAlias() + ".* from " + fromAndWhere, null);
        String pk = MysqlDdlGenerator.getPrimaryIndex(aliased.getName(), schema, instance);

        List recordList = instance.getRecordList("select count(*) as cnt from " + tableName1, null);
        long cnt = ((Number) ((Map) recordList.iterator().next()).get("cnt") ).longValue();
        while(cnt > 0) {
            // pretty dumb one-by-one copying, to avoid batch error because of PK violation.
            // if no PK present we can't guarantee that record in select and in insert will be the same!
            String orderString = pk == null ? "" : " order by " + pk;
            instance.runUpdateDataIgnore("insert into " + tableName2 + " select * from " + tableName1 + orderString + " limit 1", null);
            instance.runUpdateDataIgnore("delete from " + tableName1 + orderString + " limit 1", null);

            recordList = instance.getRecordList("select count(*) as cnt from " + tableName1, null);
            cnt = ((Number) ((Map) recordList.iterator().next()).get("cnt") ).longValue();
        }
    }

    public ArrayList<Aliased> getAllTables(String fullSelect) {
        String lowered = fullSelect.toLowerCase();
        int fromIndex = lowered.indexOf(FROM_MARKER);
        String fromAndWhere = lowered.substring(fromIndex + FROM_MARKER.length());

        return parseJoinedTables(detectFrom(fromAndWhere));
    }

    private ArrayList<Aliased> parseJoinedTables(String from) {
        from = from.toLowerCase().replace(" join ", ",").replace(" as ", "  ").replace(";", " ");
        String[] split = from.split(",");
        ArrayList<Aliased> aliases = new ArrayList<>();
        for (String s : split) {
            s = s.trim().replace(" left ", "  ").replace(" right ", "  ").replace(" outer ", "  ").replace(" inner ", "  ");
            while(s.contains("  ")) {
                s = s.replace("  ", " ");
            }
            String[] split1 = s.split(" ");
            if (split1.length > 1) {
                aliases.add(new Aliased(split1[0], split1[1]));
            }
        }
        if (aliases.isEmpty() && split.length == 1) {
            aliases.add(new Aliased(from.trim(), from.trim()));
        }
        return aliases;
    }

    public void dumpTables(String filePath) throws IOException {
        dumpTables(filePath, true);
    }

    public void dumpTables(String filePath, boolean removePrefix) throws IOException {
        List<String> tableNames = new ArrayList<>();
        List tablesByPrefix = getTablesByPrefix(prefix2);
        for (Iterator iterator = tablesByPrefix.iterator(); iterator.hasNext(); ) {
            Map next = (Map) iterator.next();
            String tableName = (String) next.get("table_name");
            tableNames.add(tableName);
        }
        dumpTables(filePath, removePrefix, tableNames);
    }

    private List getTablesByPrefix(String prefix) {
        // todo check what should be changed for non-mysql;
        String allTables = "SELECT table_name FROM information_schema.TABLES WHERE table_schema = ?" +
                " AND table_name like ? order by table_name";
        HashMap<Integer, Object> params = new HashMap<>();
        params.put(1, schema);
        params.put(2, prefix + "%");

        return helper.getRecordList(allTables, params);
    }

    public void dumpTables(String filePath, boolean removePrefix, List<String> tableNames) throws IOException {
        try (FileWriter sqlInserts = new FileWriter(filePath)) {
            for (String tableName : tableNames) {
                String tableInserts = TableDumper.getTableInserts(tableName,
                    MysqlDdlGenerator.getPrimaryIndex(tableName, schema, helper),
                    helper, TableDumper.DB_SQL);
                if (removePrefix) {
                    tableInserts = tableInserts.replace("INSERT INTO " + prefix2, "INSERT INTO ");
                }
                sqlInserts.write(tableInserts);
            }
        }
    }

    public boolean isMysql() {
        return mysql;
    }

    public void setMysql(boolean mysql) {
        this.mysql = mysql;
    }

    public void clearTempTables() {
        List tablesByPrefix = getTablesByPrefix(prefix1);
        for (Iterator iterator = tablesByPrefix.iterator(); iterator.hasNext(); ) {
            Map next = (Map) iterator.next();
            String tableName = (String) next.get("table_name");
            helper.runUpdateDataIgnore("delete from " + tableName, null);
        }
    }
}
