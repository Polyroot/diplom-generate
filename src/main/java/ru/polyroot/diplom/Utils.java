package ru.polyroot.diplom;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Slf4j
public class Utils {

    public static String getAbsolutePath(String resourcePath) {
        try (InputStream in = Utils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }
            File resourceFile = new File(resourcePath);
            File tempFile = createTempFile(resourceFile);
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.deleteOnExit(); // гарантирует удаление при выходе приложения

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Error while processing the font: " + e.getMessage(), e);
        }
    }

    private static File createTempFile(File resourceFile) throws IOException {
        String fileName = resourceFile.getName();
        String folderName = resourceFile.getParent();

        File directory = folderName != null ? new File(folderName) : new File("");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
        }

        return new File(directory, fileName);
    }

    public static File createTempDir() {
        File tempDir = FileUtils.getTempDirectory();
        File filesDir = new File(tempDir, "filesDir");
        if (!filesDir.exists()) {
            if (!filesDir.mkdirs()) {
                log.error("Failed to create directory {}", filesDir);
            }
        }
        return filesDir;
    }

    public static void cleanTempDir() {
        File tempDir = FileUtils.getTempDirectory();
        File filesDir = new File(tempDir, "filesDir");
        try {
            FileUtils.deleteDirectory(filesDir);
        } catch (IOException e) {
            log.error("Ошибка при удалении директории {}", filesDir, e);
        }
    }

}
