package dev.kensa.hamkrest.testsupport.field.xml

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.equalTo
import dev.kensa.hamkrest.testsupport.field.matching
import dev.kensa.hamkrest.testsupport.field.of
import dev.kensa.hamkrest.testsupport.field.withListOf
import dev.kensa.hamkrest.testsupport.field.withSetOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.w3c.dom.Document
import java.io.ByteArrayInputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

class XmlFieldTest {

    private val sample = parse(
        """
        <order id="42">
            <line>first</line>
            <line>second</line>
            <line>second</line>
            <customer><name>Alice</name></customer>
        </order>
        """
    )

    @Test
    fun `XmlField extracts text via XPath`() {
        val field = XmlField(compile("/order/line[1]/text()"), "First Line") { it.nodeValue }
        field.extract(sample) shouldBe "first"
    }

    @Test
    fun `XmlField applies transform to extracted node`() {
        val field = XmlField<Int>(compile("/order/@id"), "Id") { it.nodeValue.toInt() }
        field.extract(sample) shouldBe 42
    }

    @Test
    fun `XmlField returns null when XPath matches nothing`() {
        val field = XmlField(compile("/order/missing"), "Missing") { it.nodeValue }
        field.extract(sample) shouldBe null
    }

    @Test
    fun `XmlField path property exposes wrapped XPath string`() {
        val field = XmlField<String>(compile("/order/@id"), "Id") { it.nodeValue }
        field.path shouldBe "/order/@id"
    }

    @Test
    fun `XmlField path is null when XPathExpression is not wrapped`() {
        val rawXPath = XPathFactory.newInstance().newXPath().compile("/order/@id")
        val field = XmlField<String>(rawXPath, "Id") { it.nodeValue }
        field.path shouldBe null
    }

    @Test
    fun `XmlField name defaults to class simple name when null`() {
        val field = XmlField<String>({ compile("/order/@id") }, name = null) { it.nodeValue }
        field.name shouldBe "XmlField"
    }

    @Test
    fun `XmlField composes with of via core extension`() {
        val field = XmlField<String>(compile("/order/@id"), "Id") { it.nodeValue }
        (field of "42").invoke(sample) shouldBe MatchResult.Match
        val miss = field of "99"
        miss.invoke(sample).shouldBeInstanceOf<MatchResult.Mismatch>()
        miss.description shouldStartWith "Id:"
    }

    @Test
    fun `XmlTextField extracts text content of selected element`() {
        val field = XmlTextField(compile("/order/customer/name"), "Customer Name") { it }
        field.extract(sample) shouldBe "Alice"
    }

    @Test
    fun `XmlTextField applies transform after text extraction`() {
        val field = XmlTextField<Int>(compile("/order/@id"), "Id") { it.toInt() }
        field.extract(sample) shouldBe 42
    }

    @Test
    fun `XmlStringField extracts text content with identity transform`() {
        val field = XmlStringField(compile("/order/customer/name"), "Customer Name")
        field.extract(sample) shouldBe "Alice"
    }

    @Test
    fun `XmlStringField name defaults to class simple name when null`() {
        val field = XmlStringField(compile("/order/customer/name"))
        field.name shouldBe "XmlStringField"
    }

    @Test
    fun `XmlNodeField extracts the node itself`() {
        val field = XmlNodeField(compile("/order/customer"), "Customer")
        val node = field.extract(sample)
        node?.nodeName shouldBe "customer"
    }

    @Test
    fun `XmlListField path property exposes wrapped XPath string`() {
        val field = XmlListField<String>(compile("/order/line"), "Lines") { it.textContent }
        field.path shouldBe "/order/line"
    }

    @Test
    fun `XmlSetField path property exposes wrapped XPath string`() {
        val field = XmlSetField<String>(compile("/order/line"), "Lines") { it.textContent }
        field.path shouldBe "/order/line"
    }

    @Test
    fun `XmlListField extracts ordered list of transformed nodes`() {
        val field = XmlListField<String>(compile("/order/line"), "Lines") { it.textContent }
        field.extract(sample) shouldBe listOf("first", "second", "second")
    }

    @Test
    fun `XmlSetField deduplicates list and returns set of transformed values`() {
        val field = XmlSetField<String>(compile("/order/line"), "Lines") { it.textContent }
        field.extract(sample) shouldBe setOf("first", "second")
    }

    @Test
    fun `XmlListField composes with withListOf`() {
        val field = XmlListField<String>(compile("/order/line"), "Lines") { it.textContent }
        field.withListOf("first", "second", "second").invoke(sample) shouldBe MatchResult.Match
        field.withListOf("first").invoke(sample).shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `XmlSetField composes with withSetOf`() {
        val field = XmlSetField<String>(compile("/order/line"), "Lines") { it.textContent }
        field.withSetOf("second", "first").invoke(sample) shouldBe MatchResult.Match
        field.withSetOf("first").invoke(sample).shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `XmlField composes with matching using hamkrest matcher`() {
        val field = XmlStringField(compile("/order/customer/name"), "Customer Name")
        (field matching "[A-Z][a-z]+").invoke(sample) shouldBe MatchResult.Match
        (field matching "\\d+").invoke(sample).shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `XPathExpression compilation is lazy via expression provider`() {
        var compiled = 0
        val field = XmlField<String>(
            expressionProvider = {
                compiled++
                compile("/order/@id")
            },
            name = "Id"
        ) { it.nodeValue }
        compiled shouldBe 0
        field.extract(sample)
        compiled shouldBe 1
        field.extract(sample)
        compiled shouldBe 1
    }

    @Test
    fun `Node getNodeOrNull returns node when XPath matches`() {
        val xpath = compile("/order/customer/name")
        sample.getNodeOrNull(xpath)?.textContent shouldBe "Alice"
    }

    @Test
    fun `Node getNodeOrNull returns null when XPath matches nothing`() {
        sample.getNodeOrNull(compile("/order/missing")) shouldBe null
    }

    @Test
    fun `Node getNodes returns list of all matching nodes`() {
        sample.getNodes(compile("/order/line")).map { it.textContent } shouldBe listOf("first", "second", "second")
    }

    private fun compile(path: String): XPathExpression =
        XPathExpressionWrapper(XPathFactory.newInstance().newXPath().compile(path), path)

    private fun parse(xml: String): Document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(ByteArrayInputStream(xml.toByteArray()))
}
