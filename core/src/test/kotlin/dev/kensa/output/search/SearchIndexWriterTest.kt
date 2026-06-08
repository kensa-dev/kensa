package dev.kensa.output.search

import com.eclipsesource.json.Json
import dev.kensa.output.json.fakeTestContainer
import dev.kensa.output.json.fakeTestInvocation
import dev.kensa.output.json.fakeTestMethodContainer
import dev.kensa.output.json.interactionEntry
import dev.kensa.render.Renderers
import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.RenderedToken
import dev.kensa.util.NamedValue
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.paths.shouldExist
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.readText

class SearchIndexWriterTest {

    private val renderers = Renderers()

    private class SampleTest {
        @Suppress("unused") fun alpha() = Unit
        @Suppress("unused") fun beta() = Unit
    }

    private class OtherTest {
        @Suppress("unused") fun gamma() = Unit
    }

    private val sampleClass = SampleTest::class.java
    private val alpha = sampleClass.getDeclaredMethod("alpha")
    private val beta = sampleClass.getDeclaredMethod("beta")
    private val otherMethod = OtherTest::class.java.getDeclaredMethod("gamma")

    @Test
    fun `empty suite builds no terms and writes an empty index file`(@TempDir tempDir: Path) {
        SearchIndexBuilder(renderers).build(emptyList()).shouldBeEmpty()

        SearchIndexWriter().write(tempDir, emptyList())

        val file = tempDir.resolve("search-index.json")
        file.shouldExist()

        val json = Json.parse(file.readText()).asObject()
        json.getInt("schemaVersion", -1) shouldBe 1
        json.get("terms").asArray().size() shouldBe 0
    }

    @Test
    fun `single fixture produces one term with one location`() {
        val invocation = fakeTestInvocation(
            fixturesNamesAndValues = listOf(NamedValue("OrderIdFx", "acme-order-12345")),
        )
        val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
        val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

        val terms = SearchIndexBuilder(renderers).build(listOf(container))

        terms.size shouldBe 1
        val term = terms.single()
        term.value shouldBe "acme-order-12345"
        term.names shouldBe listOf("OrderIdFx")
        term.locations.size shouldBe 1
        val location = term.locations.single()
        location.testId shouldBe sampleClass.name
        location.testMethod shouldBe "alpha"
        location.invocation shouldBe 0
        location.count shouldBe 1
    }

    @Test
    fun `location carries the invocation display name preferring the parameterized description`() {
        val plain = fakeTestInvocation(
            displayName = "alpha",
            fixturesNamesAndValues = listOf(NamedValue("OrderIdFx", "acme-order-aaaaa")),
        )
        val parameterized = fakeTestInvocation(
            displayName = "alpha",
            parameterizedTestDescription = "[2] Completed",
            fixturesNamesAndValues = listOf(NamedValue("OrderIdFx", "acme-order-bbbbb")),
        )
        val method = fakeTestMethodContainer(method = alpha, invocations = listOf(plain, parameterized))
        val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

        val terms = SearchIndexBuilder(renderers).build(listOf(container))

        terms.single { it.value == "acme-order-aaaaa" }.locations.single().displayName shouldBe "alpha"
        terms.single { it.value == "acme-order-bbbbb" }.locations.single().displayName shouldBe "[2] Completed"
    }

    @Test
    fun `counts value occurrences across all rendered content in one invocation`() {
        val value = "acme-order-12345"
        val invocation = fakeTestInvocation(
            fixturesNamesAndValues = listOf(NamedValue("OrderIdFx", value)),
            sentences = listOf(
                RenderedSentence(
                    listOf(RenderedToken.RenderedValueToken(value = value, cssClasses = setOf("mv"))),
                    lineNumber = 1,
                ),
            ),
            interactions = setOf(interactionEntry(key = "Call from A to B", value = "payload $value and $value")),
        )
        val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
        val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

        val term = SearchIndexBuilder(renderers).build(listOf(container)).single { it.value == value }

        term.locations.single().count shouldBe 4
    }

    @Test
    fun `value declared in one test is located in another test that only mentions it`() {
        val value = "acme-order-12345"
        val testA = fakeTestContainer(
            testClass = SampleTest::class.java,
            methodContainers = listOf(
                fakeTestMethodContainer(
                    method = alpha,
                    invocations = listOf(fakeTestInvocation(fixturesNamesAndValues = listOf(NamedValue("OrderIdFx", value)))),
                ),
            ),
        )
        val testB = fakeTestContainer(
            testClass = OtherTest::class.java,
            methodContainers = listOf(
                fakeTestMethodContainer(
                    method = otherMethod,
                    invocations = listOf(fakeTestInvocation(interactions = setOf(interactionEntry(key = "Call from A to B", value = "mentions $value here")))),
                ),
            ),
        )

        val term = SearchIndexBuilder(renderers).build(listOf(testA, testB)).single { it.value == value }

        val testIds = term.locations.map { it.testId }
        testIds shouldBe listOf(OtherTest::class.java.name, SampleTest::class.java.name).sorted()
    }

    @Test
    fun `distinct values sharing a fixture name each carry that name`() {
        val testA = fakeTestContainer(
            testClass = SampleTest::class.java,
            methodContainers = listOf(
                fakeTestMethodContainer(
                    method = alpha,
                    invocations = listOf(fakeTestInvocation(fixturesNamesAndValues = listOf(NamedValue("OrderIdFx", "acme-order-11111")))),
                ),
            ),
        )
        val testB = fakeTestContainer(
            testClass = OtherTest::class.java,
            methodContainers = listOf(
                fakeTestMethodContainer(
                    method = otherMethod,
                    invocations = listOf(fakeTestInvocation(fixturesNamesAndValues = listOf(NamedValue("OrderIdFx", "acme-order-22222")))),
                ),
            ),
        )

        val terms = SearchIndexBuilder(renderers).build(listOf(testA, testB))

        terms.single { it.value == "acme-order-11111" }.names shouldBe listOf("OrderIdFx")
        terms.single { it.value == "acme-order-22222" }.names shouldBe listOf("OrderIdFx")
    }

    @Test
    fun `noise values are excluded but distinctive values are included`() {
        val invocation = fakeTestInvocation(
            fixturesNamesAndValues = listOf(
                NamedValue("countFx", 1),
                NamedValue("flagFx", true),
                NamedValue("statusFx", "OK"),
                NamedValue("shortFx", "abc"),
                NamedValue("zipFx", 12345),
                NamedValue("idFx", "order-abc"),
            ),
        )
        val method = fakeTestMethodContainer(method = alpha, invocations = listOf(invocation))
        val container = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(method))

        val values = SearchIndexBuilder(renderers).build(listOf(container)).map { it.value }

        values shouldBe listOf("12345", "order-abc")
    }

    @Test
    fun `terms are sorted by value and locations are sorted by testId testMethod invocation`() {
        val shared = "shared-value-9999"
        val zMethod = fakeTestMethodContainer(
            method = beta,
            invocations = listOf(
                fakeTestInvocation(fixturesNamesAndValues = listOf(NamedValue("zFx", "zzz-value-0001"))),
                fakeTestInvocation(interactions = setOf(interactionEntry(key = "Call from A to B", value = shared))),
            ),
        )
        val aMethod = fakeTestMethodContainer(
            method = alpha,
            invocations = listOf(fakeTestInvocation(fixturesNamesAndValues = listOf(NamedValue("aFx", "aaa-value-0002"), NamedValue("sFx", shared)))),
        )
        val sampleContainer = fakeTestContainer(testClass = sampleClass, methodContainers = listOf(zMethod, aMethod))
        val otherContainer = fakeTestContainer(
            testClass = OtherTest::class.java,
            methodContainers = listOf(
                fakeTestMethodContainer(method = otherMethod, invocations = listOf(fakeTestInvocation(interactions = setOf(interactionEntry(key = "Call from A to B", value = shared))))),
            ),
        )

        val terms = SearchIndexBuilder(renderers).build(listOf(otherContainer, sampleContainer))

        terms.map { it.value } shouldBe listOf("aaa-value-0002", shared, "zzz-value-0001")

        val sharedLocations = terms.single { it.value == shared }.locations
        sharedLocations.map { Triple(it.testId, it.testMethod, it.invocation) } shouldBe listOf(
            Triple(OtherTest::class.java.name, "gamma", 0),
            Triple(sampleClass.name, "alpha", 0),
            Triple(sampleClass.name, "beta", 1),
        )
    }
}
