package dev.kensa.spring

import dev.kensa.PackageDisplay
import dev.kensa.Tab
import dev.kensa.state.SetupStrategy
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource
import java.net.URI
import java.nio.file.Path

class KensaSpringPropertiesTest {

    @Test
    fun `binds all kensa properties including enums and url`() {
        val source = MapConfigurationPropertySource(
            mapOf(
                "kensa.output-dir" to "/tmp/kensa-out",
                "kensa.output-enabled" to "false",
                "kensa.title-text" to "My Report",
                "kensa.issue-tracker-url" to "https://issues.example.com",
                "kensa.tab-size" to "8",
                "kensa.auto-open-tab" to "SequenceDiagram",
                "kensa.auto-expand-notes" to "true",
                "kensa.setup-strategy" to "Grouped",
                "kensa.flatten-output-packages" to "true",
                "kensa.package-display" to "HideCommonPackages",
                "kensa.package-display-root" to "com.example",
            )
        )

        val bound = Binder(source).bind("kensa", KensaSpringProperties::class.java).get()

        bound.outputDir shouldBe Path.of("/tmp/kensa-out")
        bound.outputEnabled shouldBe false
        bound.titleText shouldBe "My Report"
        val tracker = bound.issueTrackerUrl
        tracker.shouldNotBeNull()
        tracker.toURI() shouldBe URI.create("https://issues.example.com")
        bound.tabSize shouldBe 8
        bound.autoOpenTab shouldBe Tab.SequenceDiagram
        bound.autoExpandNotes shouldBe true
        bound.setupStrategy shouldBe SetupStrategy.Grouped
        bound.flattenOutputPackages shouldBe true
        bound.packageDisplay shouldBe PackageDisplay.HideCommonPackages
        bound.packageDisplayRoot shouldBe "com.example"
    }

    @Test
    fun `relaxed enum binding accepts kebab-case and lower-case`() {
        val source = MapConfigurationPropertySource(
            mapOf(
                "kensa.setup-strategy" to "ungrouped",
                "kensa.auto-open-tab" to "sequence-diagram",
                "kensa.package-display" to "hide-common-packages",
            )
        )

        val bound = Binder(source).bind("kensa", KensaSpringProperties::class.java).get()

        bound.setupStrategy shouldBe SetupStrategy.Ungrouped
        bound.autoOpenTab shouldBe Tab.SequenceDiagram
        bound.packageDisplay shouldBe PackageDisplay.HideCommonPackages
    }

    @Test
    fun `omitted properties bind to null`() {
        val source = MapConfigurationPropertySource(
            mapOf("kensa.title-text" to "Only This")
        )

        val bound = Binder(source).bind("kensa", KensaSpringProperties::class.java).get()

        bound.titleText shouldBe "Only This"
        bound.outputDir shouldBe null
        bound.tabSize shouldBe null
        bound.setupStrategy shouldBe null
    }
}
