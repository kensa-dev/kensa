package dev.kensa

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "system-properties", mode = ResourceAccessMode.READ_WRITE)
class ConfigurationOutputPropertyTest {

    private var saved: String? = null

    @BeforeEach
    fun snapshotProperty() {
        saved = System.getProperty("kensa.disable.output")
    }

    @AfterEach
    fun restoreProperty() {
        saved.let { if (it == null) System.clearProperty("kensa.disable.output") else System.setProperty("kensa.disable.output", it) }
    }

    @Test
    fun `output is enabled when the property is absent`() {
        System.clearProperty("kensa.disable.output")

        Configuration().isOutputEnabled shouldBe true
    }

    @Test
    fun `a bare flag disables output`() {
        System.setProperty("kensa.disable.output", "")

        Configuration().isOutputEnabled shouldBe false
    }

    @Test
    fun `true disables output`() {
        System.setProperty("kensa.disable.output", "true")

        Configuration().isOutputEnabled shouldBe false
    }

    @Test
    fun `an unrecognised value disables output`() {
        System.setProperty("kensa.disable.output", "1")

        Configuration().isOutputEnabled shouldBe false
    }

    @Test
    fun `yes disables output`() {
        System.setProperty("kensa.disable.output", "yes")

        Configuration().isOutputEnabled shouldBe false
    }

    @Test
    fun `false leaves output enabled`() {
        System.setProperty("kensa.disable.output", "false")

        Configuration().isOutputEnabled shouldBe true
    }

    @Test
    fun `FALSE leaves output enabled regardless of case`() {
        System.setProperty("kensa.disable.output", "FALSE")

        Configuration().isOutputEnabled shouldBe true
    }
}
