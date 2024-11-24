package ru.polyroot.diplom;

import lombok.SneakyThrows;
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
            File tempFile = createTempFile(resourcePath);
            Files.copy(in, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            tempFile.deleteOnExit(); // гарантирует удаление при выходе приложения

            return tempFile.getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException("Error while processing the font: " + e.getMessage(), e);
        }
    }

    @SneakyThrows
    public static File createTempFile(String resourcePath) {
        File resourceFile = new File(resourcePath);
        String fileName = resourceFile.getName();
        String folderName = resourceFile.getParent();

        return new File(createTempDir(folderName), fileName);
    }

    @SneakyThrows
    public static File createTempFile(String dirName, String fileName) {
        return new File(createTempDir(dirName), fileName);
    }

    public static File createTempDir(String childTempDirPath) throws IOException {
        File tempDir = FileUtils.getTempDirectory();
        File directory = new File(tempDir, childTempDirPath);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("Failed to create directory: " + directory.getAbsolutePath());
        }
        return directory;
    }

    public static void cleanTempDir(String childTempDirPath) {
        File tempDir = FileUtils.getTempDirectory();
        File filesDir = new File(tempDir, childTempDirPath);
        try {
            FileUtils.deleteDirectory(filesDir);
        } catch (IOException e) {
            log.error("Ошибка при удалении директории {}", filesDir, e);
        }
    }

}
