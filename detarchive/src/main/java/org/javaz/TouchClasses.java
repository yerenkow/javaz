package org.javaz;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Goal which touches a timestamp file.
 *
 * @goal touch-classes
 * @phase process-sources
 */
public class TouchClasses
        extends AbstractMojo {
    /**
     * Location of the file.
     *
     * @parameter default-value="classes"
     * @required
     */
    private String classesDirectory;

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Stamp set to.
     *
     * @parameter default-value="19700101000000"
     * @required
     */
    private String stamp;

    /**
     * Stamp set to.
     *
     * @parameter default-value="yyyyMMddHHmmss"
     * @required
     */
    private String stampFormat;

    public void execute()
            throws MojoExecutionException {
        File f = outputDirectory;

        if (!f.exists()) {
            return;
        }

        File classes = new File(f, classesDirectory);
        if (classes.exists() && classes.isDirectory()) {
            Date parse = null;
            try {
                parse = new SimpleDateFormat(stampFormat).parse(stamp);
                changeDirectoryStampTo(classes, parse.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
                throw new MojoExecutionException(e.getMessage());
            }
        }
    }

    private void changeDirectoryStampTo(File classes, long time) {
        classes.setLastModified(time);
        if (classes.isDirectory()) {
            File[] files = classes.listFiles();
            for (File file : files) {
                changeDirectoryStampTo(file, time);
            }
        }
    }

}
