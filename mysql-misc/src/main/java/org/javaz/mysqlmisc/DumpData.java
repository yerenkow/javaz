package org.javaz.mysqlmisc;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.MojoExecutionException;
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
    public static final String CHECKING_TABLE_NOT_EMPTY_CASE = "(case when count(.*)>0)";
    public static final String TABLE_NAME_MATCHER = "((\\()(\\w)+)";

    /**
     * Filename for single file.
     *
     * @parameter default-value="tables"
     * @required
     */
    private String singleName;

    /**
     * Filename for log files with parsing errors.
     *
     * @parameter default-value="selectParseErrors.log"
     */
    private String selectErrorsFile;


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
    public void execute() throws MojoExecutionException {
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
        Map<String, Exception> selectsWithParsingErrors = new LinkedHashMap<>();
        for (String query : selectList) {
            Matcher caseCountMatcher = Pattern.compile(CHECKING_TABLE_NOT_EMPTY_CASE).matcher(query);
            if(caseCountMatcher.find()){
                String theGroup = caseCountMatcher.group(1);
                Matcher tableNameMatcher = Pattern.compile(TABLE_NAME_MATCHER).matcher(theGroup);
                if(tableNameMatcher.find()){
                    String tableName = tableNameMatcher.group(1).substring(1);
                    query = query.replaceAll(CHECKING_TABLE_NOT_EMPTY_CASE,
                            "case when exists (select 1 from "+ tableName + ")");
                }
            }
            try {
                replicateDataBySelect.processSelect(query);
            } catch (Exception e) {
                selectsWithParsingErrors.put(query, e);
            }
        }
        try {
            if (!outputDirectory.exists() || !outputDirectory.isDirectory()) {
                outputDirectory.mkdirs();
            }
            dumpSelectsWithErrors(new File(outputDirectory, selectErrorsFile ), selectsWithParsingErrors);
            replicateDataBySelect.dumpTables(new File(outputDirectory, singleName + sqlSuffix).getCanonicalPath(), true);
        } catch (IOException e) {
            throw new MojoExecutionException("Error dumping", e);
        }
        replicateDataBySelect.clearTempTables();
    }

    private void dumpSelectsWithErrors(File file, Map<String, Exception> selectsWithErrors) throws IOException {
        try (PrintWriter parseErrors = new PrintWriter(file)) {
            for (Map.Entry<String, Exception> entry : selectsWithErrors.entrySet()) {
                parseErrors.write(entry.getKey());
                if (entry.getValue().getMessage() != null) {
                    parseErrors.write(entry.getValue().getMessage());
                }
                entry.getValue().printStackTrace(parseErrors);
            }
        }
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

    public String getSelectErrorsFile() {
        return selectErrorsFile;
    }

    public void setSelectErrorsFile(String selectErrorsFile) {
        this.selectErrorsFile = selectErrorsFile;
    }
}
