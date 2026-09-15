package dev.kensa.output.unfurl

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

class UnfurlPageTest {

    private val url = "https://reports.example.com/index.html#/embed/default::com.example.OrderTest?method=order+is+accepted"
    private val iconUrl = "https://reports.example.com/favicon.png"
    private val page = unfurlPage(
        title = "Order is accepted & shipped",
        siteName = "Acme <Reports>",
        description = "Passed · Order Test\nGiven an order for \"2\" items\nThen it is accepted",
        url = url,
        iconUrl = iconUrl,
    )
    private val document = parse(page)

    @Test
    fun `is an html document`() {
        page shouldStartWith "<!DOCTYPE html>"
        document.documentElement.tagName shouldBe "html"
        document.getElementsByTagName("title").item(0).textContent shouldBe "Order is accepted & shipped"
    }

    @Test
    fun `carries the open graph card`() {
        meta("property", "og:type") shouldBe "article"
        meta("property", "og:site_name") shouldBe "Acme <Reports>"
        meta("property", "og:title") shouldBe "Order is accepted & shipped"
        meta("property", "og:description") shouldBe "Passed · Order Test\nGiven an order for \"2\" items\nThen it is accepted"
        meta("property", "og:url") shouldBe url
    }

    @Test
    fun `names the site icon`() {
        val link = document.getElementsByTagName("link").item(0) as Element
        link.getAttribute("rel") shouldBe "icon"
        link.getAttribute("type") shouldBe "image/png"
        link.getAttribute("href") shouldBe iconUrl
    }

    @Test
    fun `sends a person on to the embed and keeps crawlers out of the index`() {
        meta("http-equiv", "refresh") shouldBe "0; url=$url"
        meta("name", "robots") shouldBe "noindex"
        val link = document.getElementsByTagName("a").item(0) as Element
        link.getAttribute("href") shouldBe url
        link.textContent shouldBe "Order is accepted & shipped"
    }

    private fun meta(attribute: String, key: String): String {
        val metas = document.getElementsByTagName("meta")
        return (0 until metas.length)
            .map { metas.item(it) as Element }
            .single { it.getAttribute(attribute) == key }
            .getAttribute("content")
    }

    private fun parse(html: String): Document =
        DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(html.byteInputStream())
}
