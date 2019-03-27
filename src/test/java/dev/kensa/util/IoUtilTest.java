package dev.kensa.util;

import dev.kensa.KensaException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IoUtilTest {

    @Test
    void canRecreateDirectory(@TempDir Path tempDir) throws IOException {
        Path dir = createSomeFilesIn(tempDir.resolve("dir"));

        IoUtil.recreate(dir);

        assertThat(Files.isDirectory(dir)).isTrue();
        assertThat(Files.list(dir).count()).isEqualTo(0);
    }

    @Test
    void canCopyClasspathResource(@TempDir Path tempDir) throws IOException {
        Path dir = Files.createDirectory(tempDir.resolve("dir"));

        IoUtil.copyResource("/dev/kensa/util/ioutil-copy-test.txt", dir);

        String copyContent = new String(Files.readAllBytes(dir.resolve("ioutil-copy-test.txt")));
        assertThat(copyContent).isEqualTo("Some file content...");
    }

    @Test
    void copyClasspathResourceThrowsWhenDestinationNotADirectory(@TempDir Path tempDir) {
        Path destination = tempDir.resolve("foo.txt");
        assertThrows(
                KensaException.class,
                () -> IoUtil.copyResource("/dev/kensa/util/ioutil-copy-test.txt", destination)
        );
    }

    private Path createSomeFilesIn(Path dir) throws IOException {
        Path parent = Files.createDirectory(dir);
        for (int i = 1; i <= 5; i++) {
            Files.createFile(dir.resolve("file-" + i + ".dat"));
        }
        return parent;
    }
}