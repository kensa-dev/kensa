package dev.kensa;

import dev.kensa.context.TestContainer;
import dev.kensa.output.ResultWriter;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KensaJavaConfigurationTest {

    @Test
    void canDisableOutput() {
        Kensa.configure().withOutputDisabled();

        assertThat(Kensa.getConfiguration().isOutputEnabled()).isFalse();
    }

    @Test
    void throwsWhenIssueTrackerUrlInvalid() {
        assertThatThrownBy(() -> Kensa.configure().withIssueTrackerUrl("foo"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid Issue Tracker URL specified.");
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

    @Test
    void canSetResultWriter() {
        TestResultWriter resultWriter = new TestResultWriter();
        Kensa.configure().withResultWriter(resultWriter);

        assertThat(Kensa.getConfiguration().getResultWriter()).isEqualTo(resultWriter);
    }

    private static class TestResultWriter implements ResultWriter {
        @Override
        public void write(Set<TestContainer> containers) {
        }
    }

}
