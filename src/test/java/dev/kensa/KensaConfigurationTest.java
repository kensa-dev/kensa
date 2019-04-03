package dev.kensa;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KensaConfigurationTest {

    @Test
    void throwsWhenKensaOutputDirNotAbsolute() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> Kensa.configure().withOutputDir("foo"));

        assertThat(exception).hasMessage("OutputDir must be absolute.");
    }

    @Test
    void canSetKensaOutputDir() {
        Kensa.configure().withOutputDir("/foo/kensa-output");

        assertThat(Kensa.configuration().outputDir()).isEqualTo(Paths.get("/foo/kensa-output"));
    }

    @Test
    void appendsKensaOutputDirWithDirectoryWhenMissing() {
        Kensa.configure().withOutputDir("/foo");

        assertThat(Kensa.configuration().outputDir()).isEqualTo(Paths.get("/foo/kensa-output"));
    }
}
