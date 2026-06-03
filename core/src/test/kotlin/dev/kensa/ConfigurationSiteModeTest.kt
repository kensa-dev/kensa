package dev.kensa

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import kotlin.io.path.Path

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "system-properties", mode = ResourceAccessMode.READ_WRITE)
class ConfigurationSiteModeTest {

    private val managedKeys = listOf("kensa.output.root", "kensa.source.id", "kensa.source.title")
    private val savedProperties = mutableMapOf<String, String?>()

    @BeforeEach
    fun snapshotProperties() {
        managedKeys.forEach { key -> savedProperties[key] = System.getProperty(key) }
    }

    @AfterEach
    fun restoreProperties() {
        managedKeys.forEach { key ->
            val saved = savedProperties[key]
            if (saved == null) System.clearProperty(key) else System.setProperty(key, saved)
        }
    }

    @Test
    fun `dataOnly defaults to false`() {
        Configuration().dataOnly shouldBe false
    }

    @Test
    fun `with kensa source id set the outputDir nests under sources of that id and dataOnly is true`() {
        System.setProperty("kensa.output.root", "/tmp/kensa-site-test")
        System.setProperty("kensa.source.id", "uiTest")

        val config = Configuration()
        config.outputDir.toString() shouldBe "/tmp/kensa-site-test/sources/uiTest"
        config.dataOnly shouldBe true
    }

    @Test
    fun `without kensa source id the outputDir keeps the existing kensa-output suffix and dataOnly is false`() {
        System.setProperty("kensa.output.root", "/tmp/kensa-site-test")
        System.clearProperty("kensa.source.id")

        val config = Configuration()
        config.outputDir.toString() shouldBe "/tmp/kensa-site-test/kensa-output"
        config.dataOnly shouldBe false
    }

    @Test
    fun `kensa source title overrides default titleText`() {
        System.setProperty("kensa.source.title", "UI Tests")
        Configuration().titleText shouldBe "UI Tests"
    }

    @Test
    fun `without kensa source title the default titleText is used`() {
        Configuration().titleText shouldBe "Index"
    }

    @Test
    fun `withOutputDir is a no-op when dataOnly is true`() {
        System.setProperty("kensa.output.root", "/tmp/kensa-site-test")
        System.setProperty("kensa.source.id", "uiTest")

        val config = Configuration()
        val before = config.outputDir
        KensaConfigurator(config).withOutputDir(Path("/somewhere/else"))
        config.outputDir shouldBe before
    }

    @Test
    fun `withOutputDir still applies when dataOnly is false`() {
        val config = Configuration()
        KensaConfigurator(config).withOutputDir(Path("/some/build/dir"))
        config.outputDir.toString() shouldBe "/some/build/dir/kensa-output"
    }

    @Test
    fun `withProgramme and withService set the configuration values`() {
        val config = Configuration()
        KensaConfigurator(config).withProgramme("Nexus").withService("Checkout")
        config.programme shouldBe "Nexus"
        config.service shouldBe "Checkout"
    }

    @Test
    fun `programme and service default to null`() {
        val config = Configuration()
        config.programme shouldBe null
        config.service shouldBe null
    }
}
