package org.javaz.jdbc.ddl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.javaz.jdbc.util.JdbcCachedHelper;
import org.javaz.jdbc.util.JdbcHelperI;

/**
 */
public class MysqlDdlGenerator {

    private String schema;
    private String dbUrl;
    private boolean fixExistingKeyNames = true;
    public static String TYPE_VIEW = "VIEW";

    public MysqlDdlGenerator(String schema, String dbUrl) {
        this.schema = schema;
        this.dbUrl = dbUrl;
    }
    private HashMap<String, TableDdl> tableDdls = new HashMap<>();;
    private HashMap<String, IndexDdl> indexDdls = new HashMap<>();;

    public HashMap<String, TableDdl> getTableDdls() {
        return tableDdls;
    }

    public HashMap<String, IndexDdl> getIndexDdls() {
        return indexDdls;
    }



    public void buildDdls() {
        JdbcHelperI instance = JdbcCachedHelper.getInstance(dbUrl);
        List<Map> recordList = instance.getRecordList("select TABLE_NAME, TABLE_TYPE, ENGINE from INFORMATION_SCHEMA.TABLES where TABLE_SCHEMA = ? order by lower(TABLE_NAME);",
                Collections.singletonMap(1, (Object) schema));
        for (Map record : recordList) {
            buildDdl(record, instance);
        }
    }

    public void buildDdl(Map record, JdbcHelperI instance) {
        String tableName = (String) record.get("table_name");
        String tableType = (String) record.get("table_type");
        String engine = (String) record.get("engine");
        buildDdl(tableName, tableType, engine, instance);
    }

    public static String getPrimaryIndex(String tableName, String schema, JdbcHelperI instance) {
        String innodDbIndexes = "select i.name as constraint_name, t.name, i.TYPE, GROUP_CONCAT(f.name ORDER BY f.pos) as key_content from INFORMATION_SCHEMA.INNODB_SYS_INDEXES i" +
                " left join INFORMATION_SCHEMA.INNODB_SYS_TABLES t on (t.TABLE_ID = i.TABLE_ID)\n" +
                " left join INFORMATION_SCHEMA.INNODB_SYS_FIELDS f on (f.INDEX_ID = i.INDEX_ID)\n" +
                " where t.NAME = ? and i.TYPE = 3 group by i.name, t.name, i.TYPE";

        List<Map> innoKeys = instance.getRecordList(innodDbIndexes, Collections.singletonMap(1, (Object) (schema + "/" + tableName)));
        for (Map innoKey : innoKeys) {
            String keyContent = (String) innoKey.get("key_content");
            return keyContent;
        }
        return null;
    }

    public void buildDdl(String tableName, String tableType, String engine, JdbcHelperI instance) {

        TableDdl tableDdl = new TableDdl();
        tableDdl.setTableName(tableName);
        List<Map> originalCreate = instance.getRecordList("show create table " + tableName, null);
        if(!originalCreate.isEmpty()) {
            Map map = originalCreate.iterator().next();
            if (map.containsKey("create table")) {
                tableDdl.setOriginalDdl((String) map.get("create table"));
            }
            if (map.containsKey("create view")) {
                tableDdl.setOriginalDdl((String) map.get("create view"));
            }
        }

        StringBuilder builder = new StringBuilder();
        if (tableType.equals(TYPE_VIEW)) {

            String createView = tableDdl.getOriginalDdl();
            createView = "CREATE " + createView.substring(createView.indexOf(TYPE_VIEW));
            createView = createView.replace("`", "");
            builder.append(createView);

            tableDdl.setDdl(builder.toString());
            tableDdl.setView(true);
            tableDdls.put(tableName, tableDdl);
            return;
        }

        builder.append("CREATE TABLE ").append(tableName.toLowerCase()).append(" (");


        String columnsSelect = "select COLUMN_NAME, COLUMN_DEFAULT, IS_NULLABLE, COLUMN_TYPE, EXTRA from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = ? and TABLE_SCHEMA = ? order by ORDINAL_POSITION;";
        String indexesSelect = "select c.CONSTRAINT_NAME, group_concat(COLUMN_NAME order by ORDINAL_POSITION, c.POSITION_IN_UNIQUE_CONSTRAINT) as key_content, " +
                "c.REFERENCED_TABLE_NAME, group_concat(c.REFERENCED_COLUMN_NAME order by ORDINAL_POSITION, c.POSITION_IN_UNIQUE_CONSTRAINT) as REFERENCED_COLUMN_NAME, " +
                "tc.CONSTRAINT_TYPE  from " +
                "INFORMATION_SCHEMA.KEY_COLUMN_USAGE c left join INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc " +
                "on (tc.CONSTRAINT_NAME = c.CONSTRAINT_NAME and tc.TABLE_SCHEMA = c.TABLE_SCHEMA and tc.TABLE_NAME = c.TABLE_NAME and tc.CONSTRAINT_SCHEMA = c.CONSTRAINT_SCHEMA) " +
                "where c.TABLE_NAME = ? and c.TABLE_SCHEMA = ?  group by c.CONSTRAINT_NAME ," +
                "c.REFERENCED_TABLE_NAME, tc.CONSTRAINT_TYPE  order by c.CONSTRAINT_NAME, c.ORDINAL_POSITION;";

        String innodDbIndexes = "select i.name as constraint_name, i.TYPE, GROUP_CONCAT(f.name ORDER BY f.pos) as key_content from INFORMATION_SCHEMA.INNODB_SYS_INDEXES i" +
                " left join INFORMATION_SCHEMA.INNODB_SYS_TABLES t on (t.TABLE_ID = i.TABLE_ID)\n" +
                " left join INFORMATION_SCHEMA.INNODB_SYS_FIELDS f on (f.INDEX_ID = i.INDEX_ID)\n" +
                " where t.NAME = ? and i.TYPE not in (1,2,3) group by i.name, i.TYPE";
        Map<Integer, Object> params = new HashMap<>();
        params.put(1, tableName);
        params.put(2, schema);
        List<Map> columns = instance.getRecordList(columnsSelect, params);
        List<Map> indexes = instance.getRecordList(indexesSelect, params);

        List<Map> innoKeys = new ArrayList<>();
        if (engine != null && engine.equalsIgnoreCase("InnoDB")) {
            innoKeys  = instance.getRecordList(innodDbIndexes, Collections.singletonMap(1, (Object) (schema + "/" + tableName)));
        }

        boolean needComa = false;
        for (Map column : columns) {
            if (needComa) {
                builder.append(", ");
            }
            String columnName = (String) column.get("column_name");
            String columnType = (String) column.get("column_type");
            String columnDefault = (String) column.get("column_default");
            String extra = (String) column.get("extra");
            String isNullable = (String) column.get("is_nullable");
            if (columnType.contains("bigint")) {
                columnType = columnType.replaceAll("bigint\\(\\d+\\)", "bigint");
            }
            if (columnType.contains("datetime")) {
                columnType = columnType.replace("datetime", "timestamp");
            }
            if (columnType.contains("smallint")) {
                columnType = columnType.replaceAll("smallint\\(\\d+\\)", "integer");
            }
            if (columnType.contains("tinyint")) {
                columnType = columnType.replaceAll("tinyint\\(\\d+\\)", "integer");
            }
            if (columnType.startsWith("int(") || columnType.equals("int")) {
                columnType = "integer";
            }
            builder.append(columnName).append(" ").append(columnType).append(" ");
            if (extra != null && !extra.isEmpty()) {
                if (extra.equals("on update CURRENT_TIMESTAMP")) {
                    // TODO trigger ?
                } else {
                    builder.append(extra).append(" ");
                }
            }
            if (columnDefault != null) {
                if (columnDefault.equals("CURRENT_TIMESTAMP")) {
                    builder.append("DEFAULT ").append(columnDefault).append(" ");
                } else if (columnDefault.equals("0000-00-00 00:00:00")) {
                    //this is mysql-only compatible timestamp, ignore it
                } else {
                    builder.append("DEFAULT '").append(columnDefault).append("' ");
                }
            }
            if (isNullable.equals("NO")) {
                builder.append("NOT NULL ");
            }
            needComa = true;
        }
        String primaryKeys = null;
        for (Map index : indexes) {
            String constraintName = (String) index.get("constraint_name");
            if(constraintName.equals("PRIMARY")) {
                primaryKeys = (String) index.get("key_content");
            }
        }
        if (primaryKeys != null) {
            builder.append(", PRIMARY KEY (").append(primaryKeys).append(")");
            needComa = true;
        }
        for (Map index : indexes) {
            String constraintName = (String) index.get("constraint_name");
            if(!constraintName.equals("PRIMARY")) {
                String keyContent = (String) index.get("key_content");
                String constraintType = (String) index.get("constraint_type");
                if (constraintType.equals("FOREIGN KEY")) {
                    StringBuilder builder2 = new StringBuilder();
                    String referenced_table_name = (String) index.get("referenced_table_name");
                    String referenced_column_name = (String) index.get("referenced_column_name");
                    while (indexDdls.containsKey(constraintName)) {
                        constraintName += "1";
                    }
                    builder2.append("ALTER TABLE ").append(tableName).append(" ADD CONSTRAINT ").append(constraintName)
                            .append(" ").append(constraintType)
                            .append("(").append(keyContent).append(") REFERENCES ")
                            .append(referenced_table_name).append(" (").append(referenced_column_name).append(");");
                    IndexDdl indexDdl = new IndexDdl();
                    indexDdl.setDdl(builder2.toString());
                    indexDdl.setIndexName(constraintName);
                    indexDdl.setTableName(tableName);
                    indexDdl.setIndexType(constraintType);
                    indexDdl.setIndexContent(keyContent);

                    indexDdls.put(constraintName, indexDdl);
                } else if (constraintType.equals("UNIQUE")) {
                    StringBuilder builder2 = new StringBuilder();
                    while (indexDdls.containsKey(constraintName)) {
                        constraintName += "1";
                    }

                    builder2.append("ALTER TABLE ").append(tableName).append(" ADD CONSTRAINT ").append(constraintName).append(" ")
                            .append(constraintType).append(" (").append(keyContent).append(");");
                    IndexDdl indexDdl = new IndexDdl();
                    indexDdl.setDdl(builder2.toString());
                    indexDdl.setIndexName(constraintName);
                    indexDdl.setTableName(tableName);
                    indexDdl.setIndexType(constraintType);
                    indexDdl.setIndexContent(keyContent);
                    if (primaryKeys != null && primaryKeys.equals(keyContent)) {
                        indexDdl.setIgnored(true);
                    }
                    indexDdls.put(constraintName, indexDdl);
                } else {
                    System.out.println(constraintType);
                }
            }
        }
        builder.append(");");

        tableDdl.setDdl(builder.toString());
        tableDdls.put(tableName, tableDdl);

        builder = new StringBuilder();
        for (Map innoKey : innoKeys) {
            String keyContent = (String) innoKey.get("key_content");
            String constraintName = (String) innoKey.get("constraint_name");
            Integer constraintType = (Integer) innoKey.get("type");


            if (constraintType.equals(0)) {
                while (indexDdls.containsKey(constraintName)) {
                    constraintName += "1";
                }
                builder.append("CREATE INDEX ").append(constraintName).append(" ON ")
                        .append(tableName)
                        .append(" (").append(keyContent).append("); ");
                IndexDdl indexDdl = new IndexDdl();
                indexDdl.setDdl(builder.toString());
                indexDdl.setIndexName(constraintName);
                indexDdl.setTableName(tableName);
                indexDdl.setIndexType("INDEX");
                indexDdl.setIndexContent(keyContent);

                indexDdls.put(constraintName, indexDdl);
                builder = new StringBuilder();
            } else {
                System.out.println("Unexpected " + constraintType);
            }
        }
    }
}
