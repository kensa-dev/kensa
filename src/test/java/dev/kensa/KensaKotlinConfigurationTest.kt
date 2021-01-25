package dev.kensa

import dev.kensa.Kensa.configuration
import dev.kensa.Kensa.configure
import dev.kensa.Kensa.konfigure
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import java.nio.file.Paths

internal class KensaKotlinConfigurationTest {

    @Test
    internal fun outIsEnabledByDefault() {
        val configuration = Configuration()

        assertThat(configuration.isOutputEnabled).isTrue
    }

    @Test
    fun canDisableOutput() {
        konfigure {
            disableOutput()
        }

        assertThat(configuration.isOutputEnabled).isFalse
    }

    @Test
    internal fun canDisableOutputViaExplicitSystemProperty() {
        System.setProperty("kensa.disable.output", "true")

        val configuration = Configuration()
        assertThat(configuration.isOutputEnabled).isFalse

        System.clearProperty("kensa.disable.output")
    }

    @Test
    internal fun canEnableOutputViaSystemProperty() {
        System.setProperty("kensa.disable.output", "false")

        val configuration = Configuration()
        assertThat(configuration.isOutputEnabled).isTrue

        System.clearProperty("kensa.disable.output")
    }

    @Test
    internal fun outputIsEnabledUnlessPropertyIsExplicit() {
        System.setProperty("kensa.disable.output", "blah")

        val configuration = Configuration()
        assertThat(configuration.isOutputEnabled).isTrue

        System.clearProperty("kensa.disable.output")
    }

    @Test
    internal fun outputIsDisabledWhenPropertyIsPresentAndIsBlank() {
        System.setProperty("kensa.disable.output", "")

        val configuration = Configuration()
        assertThat(configuration.isOutputEnabled).isFalse

        System.clearProperty("kensa.disable.output")
    }

    @Test
    fun throwsWhenIssueTrackerUrlInvalid() {
        assertThatThrownBy { configure().withIssueTrackerUrl("foo") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Invalid Issue Tracker URL specified.")
    }

    @Test
    fun throwsWhenKensaOutputDirNotAbsolute() {
        assertThatThrownBy { configure().withOutputDir("foo") }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("OutputDir must be absolute.")
    }

    @Test
    fun canSetKensaOutputDir() {
        configure().withOutputDir("/foo/kensa-output")
        assertThat(configuration.outputDir).isEqualTo(Paths.get("/foo/kensa-output"))
    }

    @Test
    fun appendsKensaOutputDirWithDirectoryWhenMissing() {
        configure().withOutputDir("/foo")
        assertThat(configuration.outputDir).isEqualTo(Paths.get("/foo/kensa-output"))
    }
}