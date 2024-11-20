package ru.polyroot.diplom;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

@Slf4j
public class Utils {

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
