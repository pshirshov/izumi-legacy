package org.bitbucket.pshirshov.izumitk.test;

/**
 */

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 */
public final class FileUtils {

    private FileUtils() {
    }

    public static void touch(File file) throws IOException {
        // https://stackoverflow.com/questions/1406473/simulate-touch-command-with-java
        if (!file.exists()) {
            new FileOutputStream(file).close();
        }

        file.setLastModified(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public static String generateTemporaryFileName(String prefix) throws IOException {
        return generateTemporaryFileName(prefix, null);
    }

    public static File createTempFile(String prefix) throws IOException {
        return createTempFile(prefix, null);
    }


    public static File createTempFile(String prefix, String suffix) throws IOException {
        File tmpdir = tmpPath(prefix, suffix).getParent().toFile();
        File tempFile = File.createTempFile(prefix, suffix, tmpdir);
        //tempFile.deleteOnExit();
        return tempFile;
    }

    public static String generateTemporaryFileName(String prefix, String suffix) throws IOException {
        Path ret = tmpPath(prefix, suffix);

        return ret.toString();
    }

    public static String safeGenerateTemporaryFileName(String prefix) {
        return safeGenerateTemporaryFileName(prefix, null);
    }

    public static String safeGenerateTemporaryFileName(String prefix, String suffix) {
        try {
            return generateTemporaryFileName(prefix, suffix);
        } catch (IOException e) {
            throw new SkipException(); //("Can't create temporary file due to exception", e);
        }
    }

    public static File makeReadonlyTempDirectory(String prefix) {
        File tempdir = Paths.get(FileUtils.safeGenerateTemporaryFileName(prefix)).toFile();
        if (!tempdir.mkdirs()) {
            throw new IllegalStateException("Unable to create " + tempdir);
        }
        if (!tempdir.setReadOnly()) {
            throw new IllegalStateException("Unable make " + tempdir + " readonly");
        }
        return tempdir;
    }

    public static File createTempDir(Class<?> clazz) throws IOException {
        return createTempDir(clazz, null);
    }

    public static File createTempDir(Class<?> clazz, String suffix) throws IOException {
        return createTempDir(clazz.getCanonicalName(), suffix);
    }


    public static File createTempDir(String prefix) throws IOException {
        return createTempDir(prefix, null);
    }

    public static File createTempDir(String prefix, String suffix) throws IOException {
        File tmpdir = tmpPath(prefix, suffix).toFile();
        tmpdir.mkdirs();
        return tmpdir;
    }

    private static Path tmpPath(String prefix, String suffix) throws IOException {
        File f = File.createTempFile(prefix, suffix);
        f.delete();
        Path path = Paths.get(f.getCanonicalPath());
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        Path tempPath = path.getParent().resolve(FileUtils.class.getPackage().getName() + '-' + startTime);
        tempPath.toFile().mkdirs();
        final String pathAsString = tempPath.toString();
        ReusableHeavyTestResources.register(pathAsString, new ReusableTestResource<File>() {
            @Override
            public File get() {
                return new File(pathAsString);
            }

            @Override
            public void destroy() throws Exception {
                File file = get();
                if (file.exists()) {
                    org.apache.commons.io.FileUtils.forceDelete(file);
                }
            }

            @Override
            public String toString() {
                return pathAsString;
            }
        });
        return tempPath.resolve(path.getFileName());
    }

    public static void deleteRecursively(File file) throws IOException {
        org.apache.commons.io.FileUtils.deleteDirectory(file);
    }

    public static String resourceAsString(String path, Class<?> clazz) throws IOException {
        return IOUtils.toString(clazz.getResourceAsStream(path), "UTF-8");
    }
}
