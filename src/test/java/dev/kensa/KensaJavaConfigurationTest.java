package dev.kensa;

import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KensaJavaConfigurationTest {

    @Test
    void canDisableOutput() {
        Kensa.configure().withOutputDisabled();

        assertThat(Kensa.getConfiguration().isOutputEnabled()).isFalse();
    }

    @Test
    void throwsWhenKensaOutputDirNotAbsolute() {
        assertThatThrownBy(() -> Kensa.configure().withOutputDir("foo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("OutputDir must be absolute.");
    }

    @Test
    void canSetKensaOutputDir() {
        Kensa.configure().withOutputDir("/foo/kensa-output");

        assertThat(Kensa.getConfiguration().getOutputDir()).isEqualTo(Paths.get("/foo/kensa-output"));
    }

    @Test
    void appendsKensaOutputDirWithDirectoryWhenMissing() {
        Kensa.configure().withOutputDir("/foo");

        assertThat(Kensa.getConfiguration().getOutputDir()).isEqualTo(Paths.get("/foo/kensa-output"));
    }
}
