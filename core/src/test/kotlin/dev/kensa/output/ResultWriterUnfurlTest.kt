package dev.kensa.output

import dev.kensa.Configuration
import dev.kensa.output.json.fakeTestContainer
import dev.kensa.output.json.fakeTestInvocation
import dev.kensa.output.json.fakeTestMethodContainer
import dev.kensa.render.diagram.ComponentDiagramFactory
import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.RenderedToken.RenderedValueToken
import dev.kensa.state.TestState.Failed
import dev.kensa.state.TestState.Passed
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.paths.shouldNotExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.parallel.ResourceAccessMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.w3c.dom.Element
import java.net.URI
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import kotlin.io.path.readText

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock(value = "system-properties", mode = ResourceAccessMode.READ_WRITE)
class ResultWriterUnfurlTest {

    private val managedKeys = listOf("kensa.output.root", "kensa.source.id")
    private val savedProperties = mutableMapOf<String, String?>()

    @BeforeEach
    fun snapshotProperties() {
        managedKeys.forEach { key -> savedProperties[key] = System.getProperty(key) }
        managedKeys.forEach { System.clearProperty(it) }
    }

    @AfterEach
    fun restoreProperties() {
        managedKeys.forEach { key ->
            val saved = savedProperties[key]
            if (saved == null) System.clearProperty(key) else System.setProperty(key, saved)
        }
    }

    @Suppress("unused")
    private class OrderTest {
        fun orderIsAccepted() {}
        fun `order is rejected`() {}
    }

    private val orderTest = OrderTest::class.java
    private val className = orderTest.name
    private val accepted = orderTest.getDeclaredMethod("orderIsAccepted")
    private val rejected = orderTest.getDeclaredMethod("order is rejected")
    private val base = "https://reports.example.com/index.html"

    @Test
    fun `no pages are written without a linkBaseUrl`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir), ComponentDiagramFactory())

        writer.writeTest(container())

        tempDir.resolve("embed").shouldNotExist()
    }

    @Test
    fun `a page per method carries the method card and sends the reader to its embed`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir, base), ComponentDiagramFactory())

        writer.writeTest(container())

        val page = page(tempDir, "$className/orderIsAccepted.html")
        page.meta("property", "og:type") shouldBe "article"
        page.meta("property", "og:site_name") shouldBe "Order Reports"
        page.meta("property", "og:title") shouldBe "Order is accepted"
        page.meta("property", "og:description") shouldBe "Passed · Orders\nGiven an order\nThen it is accepted"
        page.meta("property", "og:url") shouldBe "$base#/embed/default::$className?method=orderIsAccepted"
        page.meta("http-equiv", "refresh") shouldBe "0; url=$base#/embed/default::$className?method=orderIsAccepted"
        page.meta("name", "robots") shouldBe "noindex"
        page.icon() shouldBe "https://reports.example.com/favicon.png"
    }

    @Test
    fun `a method named with spaces gets a page named the same way`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir, base), ComponentDiagramFactory())

        writer.writeTest(container())

        val page = page(tempDir, "$className/order is rejected.html")
        page.meta("property", "og:title") shouldBe "Order is rejected"
        page.meta("property", "og:description") shouldBe "Failed · Orders"
        page.meta("property", "og:url") shouldBe "$base#/embed/default::$className?method=order+is+rejected"
    }

    @Test
    fun `the class page carries the class state and method count`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir, base), ComponentDiagramFactory())

        writer.writeTest(container())

        val page = page(tempDir, "$className/index.html")
        page.meta("property", "og:title") shouldBe "Orders"
        page.meta("property", "og:description") shouldBe "Failed · 2 methods"
        page.meta("property", "og:url") shouldBe "$base#/embed/default::$className"
    }

    @Test
    fun `a directory base points at its index html`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir, "https://reports.example.com/latest/"), ComponentDiagramFactory())

        writer.writeTest(container())

        page(tempDir, "$className/index.html").meta("property", "og:url") shouldBe
            "https://reports.example.com/latest/index.html#/embed/default::$className"
    }

    @Test
    fun `writing the class again replaces its pages`(@TempDir tempDir: Path) {
        val writer = ResultWriter(configuration(tempDir, base), ComponentDiagramFactory())

        writer.writeTest(container())
        writer.writeTest(container(rejectedState = Passed))

        page(tempDir, "$className/index.html").meta("property", "og:description") shouldBe "Passed · 2 methods"
        page(tempDir, "$className/order is rejected.html").meta("property", "og:description") shouldBe "Passed · Orders"
    }

    @Test
    fun `a site mode source is named by its kensa source id`(@TempDir tempDir: Path) {
        System.setProperty("kensa.output.root", tempDir.toString())
        System.setProperty("kensa.source.id", "uiTest")
        val writer = ResultWriter(configuration(tempDir, base), ComponentDiagramFactory())

        writer.writeTest(container())

        val sourceDir = tempDir.resolve("sources").resolve("uiTest")
        sourceDir.resolve("results").resolve("$className.json").shouldExist()
        page(sourceDir, "$className/orderIsAccepted.html").meta("property", "og:url") shouldBe
            "$base#/embed/uiTest::$className?method=orderIsAccepted"
    }

    private fun container(rejectedState: dev.kensa.state.TestState = Failed) = fakeTestContainer(
        testClass = orderTest,
        displayName = "Orders",
        state = if (rejectedState == Failed) Failed else Passed,
        methodContainers = listOf(
            fakeTestMethodContainer(
                method = accepted,
                displayName = "Order is accepted",
                invocations = listOf(
                    fakeTestInvocation(sentences = listOf(sentence("Given", "an", "order"), sentence("Then", "it", "is", "accepted"))),
                    fakeTestInvocation(sentences = listOf(sentence("Given", "another", "order"))),
                ),
            ),
            fakeTestMethodContainer(method = rejected, displayName = "Order is rejected", state = rejectedState),
        ),
    )

    private fun sentence(vararg words: String) = RenderedSentence(words.map { RenderedValueToken(it, setOf("wd")) }, lineNumber = 1)

    private fun configuration(dir: Path, linkBase: String? = null) = Configuration().apply {
        outputDir = dir
        dataOnly = true
        titleText = "Order Reports"
        linkBase?.let { linkBaseUrl = URI(it).toURL() }
    }

    private class Page(private val metas: List<Element>, private val links: List<Element>) {
        fun meta(attribute: String, key: String): String = metas.single { it.getAttribute(attribute) == key }.getAttribute("content")
        fun icon(): String = links.single { it.getAttribute("rel") == "icon" }.getAttribute("href")
    }

    private fun page(dir: Path, path: String): Page {
        val file = dir.resolve("embed").resolve(path)
        file.shouldExist()
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file.readText().byteInputStream())
        val metas = document.getElementsByTagName("meta")
        val links = document.getElementsByTagName("link")
        return Page((0 until metas.length).map { metas.item(it) as Element }, (0 until links.length).map { links.item(it) as Element })
    }
}
