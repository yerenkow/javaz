package org.javaz.mysqlmisc;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;

/**
 */
public abstract class AbstractBaseSql extends AbstractMojo {
    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}/sql"
     * @required
     */
    protected File outputDirectory;

    /**
     * Suffix for sql.
     *
     * @parameter default-value=".sql"
     * @required
     */
    protected String sqlSuffix;

    /**
     * Schema Name.
     *
     * @parameter
     * @required
     */
    protected String schemaName;

    /**
     * jdbc url.
     *
     * @parameter
     * @required
     */
    protected String jdbcUrl;

    public File getOutputDirectory() {
        return outputDirectory;
    }

    public void setOutputDirectory(File outputDirectory) {
        this.outputDirectory = outputDirectory;
    }

    public String getSqlSuffix() {
        return sqlSuffix;
    }

    public void setSqlSuffix(String sqlSuffix) {
        this.sqlSuffix = sqlSuffix;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }
}
