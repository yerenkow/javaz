package org.javaz.mysqlmisc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.javaz.jdbc.util.ReplicateDataBySelect;

/**
 * Goal which dumps database content as inserts
 *
 * @goal dumpdml
 * @phase install
 */
public class DumpData extends AbstractBaseSql {
    public static final String P_6_SPY = "p6spy";
    public static final int P_6_FULL_LENGTH = 6;
    public static final int P_6_SMALL_LENGTH = 5;

    /**
     * Filename for single file.
     *
     * @parameter default-value="tables"
     * @required
     */
    private String singleName;

    /**
     * Suffix for sql.
     *
     * @parameter default-value=".sql"
     * @required
     */
    private String sqlSuffix;

    /**
     * Whether each table should be in separate file.
     *
     * @parameter default-value="false"
     * @required
     */
    private boolean filePerTable;

    /**
     * Tables to be dumped.
     *
     * @parameter
     */
    private String[] tableNames;

    /**
     * Selects, based on which data extracted to be dumped.
     *
     * @parameter
     */
    private String[] selects;

    /**
     * SQL log file, where we should find queries
     *
     * @parameter
     */
    private File sqlLogFile;

    /**
     * Format of log file, only p6spy or plain allowed.
     *
     * @parameter default-value="p6spy"
     */
    private String sqlLogFormat;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        ArrayList<String> selectList = new ArrayList();
        if (selects != null) {
            for (String select : selects) {
                selectList.add(select);
            }
        }
        if (tableNames != null) {
            for (String tableName : tableNames) {
                selectList.add("select * from " + tableName + ";");
            }
        }
        if (sqlLogFile != null) {
            LineNumberReader lnr = null;
            String line = null;
            try {
                lnr = new LineNumberReader(new FileReader(sqlLogFile));
                while((line = lnr.readLine()) != null) {
                    String filteredQuery = filter(line);
                    selectList.add(filteredQuery);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (lnr != null) {
                    try {
                        lnr.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        ReplicateDataBySelect replicateDataBySelect = new ReplicateDataBySelect(schemaName, jdbcUrl);
        replicateDataBySelect.clearTempTables();
        for (String query : selectList) {
            replicateDataBySelect.processSelect(query);
        }
        try {
            if (!outputDirectory.exists() || !outputDirectory.isDirectory()) {
                outputDirectory.mkdirs();
            }
            replicateDataBySelect.dumpTables(new File(outputDirectory, singleName + sqlSuffix).getCanonicalPath(), true);
        } catch (IOException e) {
            throw new MojoExecutionException("Error dumping", e);
        }
        replicateDataBySelect.clearTempTables();
    }

    private String filter(String line) {
        if (sqlLogFormat.equals(P_6_SPY)) {
            String[] split = line.split("\\|");
            if (split.length == P_6_FULL_LENGTH || split.length == P_6_SMALL_LENGTH) {
                return split[split.length - 1];
            }
        }
        return line;
    }

    public String getSingleName() {
        return singleName;
    }

    public void setSingleName(String singleName) {
        this.singleName = singleName;
    }

    @Override
    public String getSqlSuffix() {
        return sqlSuffix;
    }

    @Override
    public void setSqlSuffix(String sqlSuffix) {
        this.sqlSuffix = sqlSuffix;
    }

    public boolean isFilePerTable() {
        return filePerTable;
    }

    public void setFilePerTable(boolean filePerTable) {
        this.filePerTable = filePerTable;
    }

    public String[] getTableNames() {
        return tableNames;
    }

    public void setTableNames(String[] tableNames) {
        this.tableNames = tableNames;
    }

    public String[] getSelects() {
        return selects;
    }

    public void setSelects(String[] selects) {
        this.selects = selects;
    }

    public File getSqlLogFile() {
        return sqlLogFile;
    }

    public void setSqlLogFile(File sqlLogFile) {
        this.sqlLogFile = sqlLogFile;
    }

    public String getSqlLogFormat() {
        return sqlLogFormat;
    }

    public void setSqlLogFormat(String sqlLogFormat) {
        this.sqlLogFormat = sqlLogFormat;
    }
}
