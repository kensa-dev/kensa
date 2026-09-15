package dev.kensa

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.net.URI

class ConfigurationLinkBaseUrlTest {

    private val configured = URI("https://reports.example.com/index.html").toURL()

    @Test
    fun `linkBaseUrl defaults to null`() {
        Configuration().linkBaseUrl shouldBe null
    }

    @Test
    fun `withLinkBaseUrl sets it`() {
        val config = Configuration()
        KensaConfigurator(config).withLinkBaseUrl(configured)
        config.linkBaseUrl shouldBe configured
    }
}
