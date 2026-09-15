package dev.kensa.output.unfurl

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.net.URI

class EmbedUrlTest {

    private val base = URI("https://ci.example.com/build/.lastSuccessful/kensa-output/index.html").toURL()

    @Test
    fun `a standalone report is source default, as the UI fallback manifest names it`() {
        embedUrl(base, "default", "com.example.OrderTest", "orderIsAccepted") shouldBe
            "https://ci.example.com/build/.lastSuccessful/kensa-output/index.html#/embed/default::com.example.OrderTest?method=orderIsAccepted"
    }

    @Test
    fun `a site mode source keeps its own id`() {
        embedUrl(base, "uiTest", "com.example.OrderTest", "orderIsAccepted") shouldBe
            "https://ci.example.com/build/.lastSuccessful/kensa-output/index.html#/embed/uiTest::com.example.OrderTest?method=orderIsAccepted"
    }

    @Test
    fun `the method name is url encoded the way the report encodes it`() {
        embedUrl(base, "default", "com.example.OrderTest", "order is accepted & shipped") shouldBe
            "https://ci.example.com/build/.lastSuccessful/kensa-output/index.html#/embed/default::com.example.OrderTest?method=order+is+accepted+%26+shipped"
    }

    @Test
    fun `a base ending in a slash points at its index html`() {
        embedUrl(URI("https://reports.example.com/latest/").toURL(), "default", "com.example.OrderTest", "orderIsAccepted") shouldBe
            "https://reports.example.com/latest/index.html#/embed/default::com.example.OrderTest?method=orderIsAccepted"
    }

    @Test
    fun `no method gives the class url without a query`() {
        embedUrl(base, "default", "com.example.OrderTest", null) shouldBe
            "https://ci.example.com/build/.lastSuccessful/kensa-output/index.html#/embed/default::com.example.OrderTest"
    }

    @Test
    fun `the icon sits beside a base ending in a slash`() {
        iconUrl(URI("https://reports.example.com/latest/").toURL()) shouldBe "https://reports.example.com/latest/favicon.png"
    }

    @Test
    fun `the icon sits beside a base ending in index html`() {
        iconUrl(base) shouldBe "https://ci.example.com/build/.lastSuccessful/kensa-output/favicon.png"
    }

    @Test
    fun `the icon sits beside a base ending in some other file`() {
        iconUrl(URI("https://reports.example.com/latest/report.html").toURL()) shouldBe "https://reports.example.com/latest/favicon.png"
    }
}
