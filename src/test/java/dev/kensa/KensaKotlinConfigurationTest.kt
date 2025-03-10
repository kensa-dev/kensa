package dev.kensa

import dev.kensa.Kensa.configuration
import dev.kensa.Kensa.configure
import dev.kensa.Kensa.konfigure
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Test
import java.nio.file.Paths

internal class KensaKotlinConfigurationTest {

    @Test
    internal fun outIsEnabledByDefault() {
        val configuration = Configuration()

        configuration.isOutputEnabled.shouldBeTrue()
    }

    @Test
    fun canDisableOutput() {
        konfigure {
            disableOutput()
        }

        configuration.isOutputEnabled.shouldBeFalse()
    }

    @Test
    internal fun canDisableOutputViaExplicitSystemProperty() {
        System.setProperty("kensa.disable.output", "true")

        val configuration = Configuration()
        configuration.isOutputEnabled.shouldBeFalse()

        System.clearProperty("kensa.disable.output")
    }

    @Test
    internal fun canEnableOutputViaSystemProperty() {
        System.setProperty("kensa.disable.output", "false")

        val configuration = Configuration()
        configuration.isOutputEnabled.shouldBeTrue()

        System.clearProperty("kensa.disable.output")
    }

    @Test
    internal fun outputIsEnabledUnlessPropertyIsExplicit() {
        System.setProperty("kensa.disable.output", "blah")

        val configuration = Configuration()
        configuration.isOutputEnabled.shouldBeTrue()

        System.clearProperty("kensa.disable.output")
    }

    @Test
    internal fun outputIsDisabledWhenPropertyIsPresentAndIsBlank() {
        System.setProperty("kensa.disable.output", "")

        val configuration = Configuration()
        configuration.isOutputEnabled.shouldBeFalse()

        System.clearProperty("kensa.disable.output")
    }

    @Test
    fun throwsWhenKensaOutputDirNotAbsolute() {
        shouldThrowExactly<IllegalArgumentException> { configure().withOutputDir("foo") }
            .shouldHaveMessage("OutputDir must be absolute.")
    }

    @Test
    fun canSetKensaOutputDir() {
        configure().withOutputDir("/foo/kensa-output")
        configuration.outputDir shouldBe Paths.get("/foo/kensa-output")
    }

    @Test
    fun appendsKensaOutputDirWithDirectoryWhenMissing() {
        configure().withOutputDir("/foo")
        configuration.outputDir shouldBe Paths.get("/foo/kensa-output")
    }
}