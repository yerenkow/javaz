package org.javaz.jdbc.ddl;

/**
 */
public class IndexDdl {
    private String indexName;
    private String indexContent;
    private String indexType;
    private String tableName;
    private String ddl;
    private String originalDdl;
    private boolean ignored = false;

    public IndexDdl() {
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

    public String getIndexContent() {
        return indexContent;
    }

    public void setIndexContent(String indexContent) {
        this.indexContent = indexContent;
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

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(String indexType) {
        this.indexType = indexType;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }
}
