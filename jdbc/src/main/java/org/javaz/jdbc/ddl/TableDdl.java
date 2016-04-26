package org.javaz.jdbc.ddl;

/**
 */
public class TableDdl {
    private String tableName;
    private String ddl;
    private String originalDdl;
    private boolean view = false;

    public TableDdl(String tableName, String ddl) {
        this.tableName = tableName;
        this.ddl = ddl;
    }

    public TableDdl() {
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getDdl() {
        return ddl;
    }

    public void setDdl(String ddl) {
        this.ddl = ddl;
    }

    public String getOriginalDdl() {
        return originalDdl;
    }

    public void setOriginalDdl(String originalDdl) {
        this.originalDdl = originalDdl;
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }
}
