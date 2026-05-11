package dev.kensa.spring

import dev.kensa.Configuration
import dev.kensa.Kensa
import dev.kensa.PackageDisplay
import dev.kensa.Tab
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD
import org.springframework.core.env.StandardEnvironment
import org.springframework.mock.env.MockPropertySource
import java.net.URI
import java.nio.file.Path

@Execution(SAME_THREAD)
class KensaSpringExtensionTest {

    private val snapshot = configurationSnapshot()

    @AfterEach
    fun restore() {
        Kensa.konfigure { snapshot.restoreTo(this) }
    }

    @Test
    fun `applyFrom copies only non-null properties onto a Configuration`() {
        val target = Configuration()
        val props = KensaSpringProperties(
            outputDir = Path.of("/tmp/foo"),
            titleText = "Hello",
            tabSize = 9,
            setupStrategy = SetupStrategy.Grouped,
            packageDisplay = PackageDisplay.Hidden,
            autoOpenTab = Tab.CapturedOutputs,
        )

        target.applyFrom(props)

        target.outputDir shouldBe Path.of("/tmp/foo")
        target.titleText shouldBe "Hello"
        target.tabSize shouldBe 9
        target.setupStrategy shouldBe SetupStrategy.Grouped
        target.packageDisplay shouldBe PackageDisplay.Hidden
        target.autoOpenTab shouldBe Tab.CapturedOutputs
    }

    @Test
    fun `applyFrom leaves untouched fields at their existing values`() {
        val target = Configuration().apply { titleText = "Original" }
        val props = KensaSpringProperties(tabSize = 42)

        target.applyFrom(props)

        target.titleText shouldBe "Original"
        target.tabSize shouldBe 42
    }

    @Test
    fun `applyKensaProperties mutates the global Kensa configuration`() {
        val env = StandardEnvironment().apply {
            propertySources.addFirst(
                MockPropertySource("kensa-test").apply {
                    setProperty("kensa.title-text", "Spring Bound")
                    setProperty("kensa.issue-tracker-url", "https://kensa.example.com")
                    setProperty("kensa.setup-strategy", "Grouped")
                }
            )
        }

        applyKensaProperties(env)

        Kensa.konfigure {
            titleText shouldBe "Spring Bound"
            issueTrackerUrl.toURI() shouldBe URI.create("https://kensa.example.com")
            setupStrategy shouldBe SetupStrategy.Grouped
        }
    }
}

private data class ConfigurationSnapshot(
    val outputDir: Path,
    val isOutputEnabled: Boolean,
    val titleText: String,
    val issueTrackerUrl: java.net.URL,
    val tabSize: Int,
    val autoOpenTab: Tab,
    val autoExpandNotes: Boolean,
    val setupStrategy: SetupStrategy,
    val flattenOutputPackages: Boolean,
    val packageDisplay: PackageDisplay,
    val packageDisplayRoot: String?,
) {
    fun restoreTo(target: Configuration) {
        target.outputDir = outputDir
        target.isOutputEnabled = isOutputEnabled
        target.titleText = titleText
        target.issueTrackerUrl = issueTrackerUrl
        target.tabSize = tabSize
        target.autoOpenTab = autoOpenTab
        target.autoExpandNotes = autoExpandNotes
        target.setupStrategy = setupStrategy
        target.flattenOutputPackages = flattenOutputPackages
        target.packageDisplay = packageDisplay
        target.packageDisplayRoot = packageDisplayRoot
    }
}

private fun configurationSnapshot(): ConfigurationSnapshot {
    var snap: ConfigurationSnapshot? = null
    Kensa.konfigure {
        snap = ConfigurationSnapshot(
            outputDir = outputDir,
            isOutputEnabled = isOutputEnabled,
            titleText = titleText,
            issueTrackerUrl = issueTrackerUrl,
            tabSize = tabSize,
            autoOpenTab = autoOpenTab,
            autoExpandNotes = autoExpandNotes,
            setupStrategy = setupStrategy,
            flattenOutputPackages = flattenOutputPackages,
            packageDisplay = packageDisplay,
            packageDisplayRoot = packageDisplayRoot,
        )
    }
    return snap!!
}
