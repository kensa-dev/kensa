package dev.kensa

import dev.kensa.sentence.Keyword
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.antlr.v4.runtime.atn.PredictionMode
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class ConfigurationApiConsistencyTest {

    @Test
    fun `keywords added through the DSL reach the dictionary`() {
        val config = Configuration().apply { keywords("frobnicates") }

        config.dictionary.keywords.any { it.value == "frobnicates" } shouldBe true
    }

    @Test
    fun `keyword instances added through the DSL reach the dictionary`() {
        val config = Configuration().apply { keywords(Keyword("wibbles")) }

        config.dictionary.keywords.any { it.value == "wibbles" } shouldBe true
    }

    @Test
    fun `the configurator can set titleText`() {
        val config = Configuration()

        KensaConfigurator(config).withTitleText("Acceptance Tests")

        config.titleText shouldBe "Acceptance Tests"
    }

    @Test
    fun `the configurator can set the antlr prediction mode`() {
        val config = Configuration()

        KensaConfigurator(config).withAntlrPredicationMode(PredictionMode.SLL)

        config.antlrPredicationMode shouldBe PredictionMode.SLL
    }

    @Test
    fun `the configurator can enable the antlr error listener`() {
        val config = Configuration()

        KensaConfigurator(config).withAntlrErrorListenerDisabled(false)

        config.antlrErrorListenerDisabled shouldBe false
    }

    @Test
    fun `a section order missing a section is rejected`() {
        val config = Configuration()

        shouldThrow<IllegalArgumentException> {
            config.sectionOrder = listOf(Section.Tabs, Section.Sentences)
        }
    }

    @Test
    fun `a section order repeating a section is rejected`() {
        val config = Configuration()

        shouldThrow<IllegalArgumentException> {
            config.sectionOrder = listOf(Section.Tabs, Section.Tabs, Section.Sentences)
        }
    }

    @Test
    fun `a section order covering every section is accepted`() {
        val config = Configuration()

        config.sectionOrder = Section.entries.toList()

        config.sectionOrder shouldBe Section.entries.toList()
    }

    // A relative outputDir is deliberately accepted by the property setter, unlike
    // KensaConfigurator.withOutputDir which requires an absolute path. ResultWriter resolves
    // it for the banner, and ResultWriterBannerTest covers that.
    @Test
    fun `a relative outputDir is accepted by the property setter`() {
        val config = Configuration()

        config.outputDir = Path("relative/output")

        config.outputDir.toString() shouldBe "relative/output"
    }
}
