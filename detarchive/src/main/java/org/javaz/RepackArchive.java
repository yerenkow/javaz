package org.javaz;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.*;
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
    private String archiveSuffix;

    /**
     * Which archive to process.
     *
     * @parameter
     */
    private File archiveName;

    /**
     * Which files to skip.
     *
     * @parameter
     */
    private String[] skipFiles;

    /**
     * Which files to skip.
     *
     * @parameter default-value="true"
     */
    private boolean skipPomProperties;

    /**
     * Stamp set to.
     *
     * @parameter
     */
    private String stamp;

    /**
     * Stamp set to.
     *
     * @parameter default-value="yyyyMMddHHmmss"
     * @required
     */
    private String stampFormat;

    /**
     * Stamp set to.
     *
     * @parameter default-value="META-INF/MANIFEST.MF"
     * @required
     */
    private String manifestName;

    public void execute() throws MojoExecutionException {
        JarInputStream jis = null;
        JarOutputStream jos = null;

        try {
            if(archiveName == null) {
                //let's find one.
                if(outputDirectory.isDirectory()) {
                    File[] files = outputDirectory.listFiles();
                    for (int i = 0; archiveName == null && i < files.length; i++) {
                        File file = files[i];
                        if(!file.isDirectory() && file.getName().endsWith("ar")) {
                            //any *ar will do.
                            archiveName = file;
                            getLog().info("Found " + file.getName() + " as archive, going to repack it.");
                        }
                    }
                }

                if(archiveName == null) {
                    getLog().info("Found zero archives, nothing to do.");
                    return;
                }
            }

            HashSet<String> skips = new HashSet<String>();
            if(skipFiles != null) {
                for (String toSkip : skipFiles) {
                    skips.add(toSkip);
                }
            }
            if(skipPomProperties) {
                skips.add("pom.properties");
            }
            getLog().info("Found " + skips.size() + " file names for skip" + (!skips.isEmpty() && skips.size() < 10 ? " = " + skips : "") + ".");

            long time = -1;
            try {
                if(stamp != null) {
                    time = new SimpleDateFormat(stampFormat).parse(stamp).getTime();
                }
            } catch (ParseException e) {
                e.printStackTrace();
                throw new MojoExecutionException(e.getMessage());
            }
            jis = new JarInputStream(new FileInputStream(archiveName));
            String modifiedName = archiveName.getAbsolutePath();
            modifiedName = modifiedName.substring(0, modifiedName.lastIndexOf(".")) + archiveSuffix + modifiedName.substring(modifiedName.lastIndexOf("."));
            getLog().info("Going repack " + archiveName + " with suffix '" + archiveSuffix + " into " + modifiedName);
            ArrayList<String> names = new ArrayList<String>();
            ZipEntry nextEntry = null;
            HashMap<Long, Integer> countsOfTime = new HashMap<Long, Integer>();
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
                    if(!nextEntry.isDirectory() && name.endsWith(".class")) {
                        // if not directory, we need to write time;
                        writeTime(countsOfTime, nextEntry.getTime());
                    }
                }
            }
            Manifest manifest = jis.getManifest();

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            manifest.write(out);
            byte[] manifestArray = out.toByteArray();
            jis.close();

            names.add(manifestName);
            Collections.sort(names);
            if(time == -1) {
                time = winnerTime(countsOfTime);
            }

            jos = new JarOutputStream(new FileOutputStream(modifiedName));

            byte[] buffer = new byte[4096];

            JarFile jin = new JarFile(archiveName);
            for (String name : names) {
                if(name.equals(manifestName)) {
                    ZipEntry zipEntry = new ZipEntry(manifestName);
                    zipEntry.setTime(time);
                    jos.putNextEntry(zipEntry);
                    jos.write(manifestArray);
                    continue;
                }
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
            jin.close();
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

    private long winnerTime(HashMap<Long, Integer> countsOfTime) {
        long mostFreq = 0L;
        int mostFreqCount = 0;
        Set<Long> longs = countsOfTime.keySet();
        for (Long aLong : longs) {
            Integer cnt = countsOfTime.get(aLong);
            if(mostFreqCount < cnt) {
                mostFreqCount = cnt;
                mostFreq = aLong;
            }
        }
        return mostFreq;
    }

    private void writeTime(HashMap<Long, Integer> countsOfTime, long time) {
        if(countsOfTime.containsKey(time)) {
            countsOfTime.put(time, countsOfTime.get(time) + 1);
        } else {
            countsOfTime.put(time, 1);
        }
    }
}

