package org.javaz;

import org.apache.maven.plugin.AbstractMojo;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Goal which tries to make JARs/WARs deterministic
 *
 * @goal repack
 * @phase package
 */
public class RepackArchive extends AbstractMojo {

    /**
     * Location of the file.
     *
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Location of the file.
     *
     * @parameter default-value="-det"
     * @required
     */
    private File prefix;

    /**
     * Which archive to process.
     *
     * @parameter
     * @required
     */
    private File archive;

    /**
     * Which files to skip.
     *
     * @parameter default-value=[]
     * @required
     */
    private String[] skipFiles;

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

    public void execute() {
        JarInputStream jis = null;
        JarOutputStream jos = null;

        try {
            HashSet<String> skips = new HashSet<String>();
            if(skipFiles != null) {
                for (String toSkip : skipFiles) {
                    skips.add(toSkip);
                }
            }
            getLog().info("Found " + skips.size() + " file names for skip.");

            long time = 0;
            try {
                time = new SimpleDateFormat(stampFormat).parse(stamp).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }

            jis = new JarInputStream(new FileInputStream(archive));
            String modifiedName = archive.getAbsolutePath();
            modifiedName = modifiedName.substring(0, modifiedName.lastIndexOf(".")) + prefix + modifiedName.substring(modifiedName.lastIndexOf("."));
            ArrayList<String> names = new ArrayList<String>();
            ZipEntry nextEntry = null;
            while((nextEntry = jis.getNextEntry()) != null ) {

                ZipEntry zipEntry = new ZipEntry(nextEntry);
                String name = zipEntry.getName();
                String shortName = name;
                if(shortName.contains("/")) {
                    shortName = shortName.substring(shortName.lastIndexOf("/") + 1);
                }
                boolean skip = skips.contains(name) || skips.contains(shortName);
                if(skip) {
                    getLog().info("Skipping " + name + " as it is listed in skipFiles");
                } else {
                    names.add(name);
                }
            }
            jis.close();
            Collections.sort(names);

            jos = new JarOutputStream(new FileOutputStream(modifiedName));
            byte[] buffer = new byte[4096];

            JarFile jin = new JarFile(archive);
            for (String name : names) {
                nextEntry = jin.getEntry(name);
                InputStream inputStream = jin.getInputStream(nextEntry);

                ZipEntry zipEntry = new ZipEntry(nextEntry);
                zipEntry.setTime(time);
                jos.putNextEntry(zipEntry);
                if(!zipEntry.isDirectory()) {
                    while (inputStream.available() > 0){
                        int read = inputStream.read(buffer);
                        if(read != -1) {
                            jos.write(buffer,0,read);
                        }
                    }
                }
                inputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(jos != null)
                    jos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

