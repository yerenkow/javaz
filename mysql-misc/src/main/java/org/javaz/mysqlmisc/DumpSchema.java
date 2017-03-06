package org.javaz.mysqlmisc;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.javaz.jdbc.ddl.IndexDdl;
import org.javaz.jdbc.ddl.MysqlDdlGenerator;
import org.javaz.jdbc.ddl.TableDdl;
import org.javaz.jdbc.util.ReplicateDataBySelect;

/**
 * Goal which dumps database structure to separate files
 *
 * @goal dumpddl
 * @phase install
 *
 */
public class DumpSchema extends AbstractBaseSql {

    /**
     * Subdir for table.
     *
     * @parameter default-value="table"
     * @required
     */
    protected String tableSubdir;

    /**
     * Subdir for view.
     *
     * @parameter default-value="view"
     * @required
     */
    protected String viewSubdir;

    /**
     * Filename for index file.
     *
     * @parameter default-value="indexes.sql"
     * @required
     */
    protected String indexFile;

    /**
     * Whether old files should be deleted.
     *
     * @parameter default-value="false"
     * @required
     */
    protected boolean deleteOld;


    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        MysqlDdlGenerator generator = new MysqlDdlGenerator(schemaName, jdbcUrl);
        generator.buildDdls();
        Map<String, TableDdl> ddls = generator.getTableDdls();
        File viewSubdirFile = null;
        File tableSubdirFile = null;

        HashSet<String> toRemoveTables = new HashSet<>();
        HashSet<String> toRemoveViews = new HashSet<>();

        try {
            if (!outputDirectory.exists() || !outputDirectory.isDirectory()) {
                outputDirectory.mkdirs();
            }
            viewSubdirFile = new File(outputDirectory, viewSubdir);
            viewSubdirFile.mkdirs();
            tableSubdirFile = new File(outputDirectory, tableSubdir);
            tableSubdirFile.mkdirs();

            toRemoveTables = listFiles(tableSubdirFile);
            toRemoveViews = listFiles(viewSubdirFile);

            for (String tableName : ddls.keySet()) {
                TableDdl ddl = ddls.get(tableName);
                boolean isView = ddl.isView();
                File parent = isView ? viewSubdirFile : tableSubdirFile;;
                String fileName = tableName.toLowerCase() + sqlSuffix;
                (isView ? toRemoveViews : toRemoveTables).remove(fileName);
                try (FileWriter fileWriter = new FileWriter(new File(parent, fileName))) {
                    fileWriter.write(ddl.getDdl());
                    fileWriter.write("\n");
                    String originalDdl = ddl.getOriginalDdl();
                    if (originalDdl != null) {
                        String[] split = originalDdl.split("\n");
                        for (String s : split) {
                            fileWriter.write("--" + s + "\n");
                        }
                    }
                }
            }
            HashMap<String, IndexDdl> indexDdls = generator.getIndexDdls();
            ArrayList<String> indexNames = new ArrayList<>(indexDdls.keySet());
            Collections.sort(indexNames);
            try (FileWriter fileWriter = new FileWriter(new File(outputDirectory, indexFile))) {
                for (String indexName : indexNames) {
                    IndexDdl indexDdl = indexDdls.get(indexName);
                    if (indexDdl.getDdl().contains(ReplicateDataBySelect.prefix1) ||
                            indexDdl.getDdl().contains(ReplicateDataBySelect.prefix2)) {
                        continue;
                    }
                    fileWriter.write(indexDdl.isIgnored() ? "--" : "");
                    fileWriter.write(indexDdl.getDdl());
                    fileWriter.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (deleteOld) {
            for (String toRemove : toRemoveViews) {
                new File(viewSubdirFile, toRemove).delete();
            }
            for (String toRemove : toRemoveTables) {
                new File(tableSubdirFile, toRemove).delete();
            }
        }
    }

    private HashSet<String> listFiles(File dir) {
        HashSet<String> toRemove = new HashSet<>();
        String[] list = (dir.exists() && dir.isDirectory()) ? dir.list() : new String[0];
        for (String fileName : list) {
            toRemove.add(fileName);
        }
        return toRemove;
    }

    public String getTableSubdir() {
        return tableSubdir;
    }

    public void setTableSubdir(String tableSubdir) {
        this.tableSubdir = tableSubdir;
    }

    public String getViewSubdir() {
        return viewSubdir;
    }

    public void setViewSubdir(String viewSubdir) {
        this.viewSubdir = viewSubdir;
    }

    public String getIndexFile() {
        return indexFile;
    }

    public void setIndexFile(String indexFile) {
        this.indexFile = indexFile;
    }

    public boolean isDeleteOld() {
        return deleteOld;
    }

    public void setDeleteOld(boolean deleteOld) {
        this.deleteOld = deleteOld;
    }
}
