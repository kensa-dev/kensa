package dev.kensa.output.json

import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.RenderedToken
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class IndexMetricsTest {
    private class SampleTest { @Suppress("unused") fun alpha() = Unit }
    private val alpha = SampleTest::class.java.getDeclaredMethod("alpha")

    private fun kw(value: String) = RenderedToken.RenderedValueToken(value, setOf("tk-kw"))
    private fun word(value: String) = RenderedToken.RenderedValueToken(value, setOf("tk-wd"))
    private fun expandable(name: String) = RenderedToken.RenderedExpandableToken(name, setOf("tk-ex"), null, name, emptyList(), emptyList())
    private fun sentence(vararg tokens: RenderedToken) = RenderedSentence(tokens.toList(), 1)

    @Test
    fun `counts interactions across invocations`() {
        val container = fakeTestMethodContainer(
            method = alpha,
            invocations = listOf(
                fakeTestInvocation(interactions = setOf(interactionEntry("Request from A to B"), interactionEntry("Response from B to A"))),
                fakeTestInvocation(interactions = setOf(interactionEntry("Ping from A to C"))),
            ),
        )

        IndexMetrics.interactionCount(container) shouldBe 3
    }

    @Test
    fun `counts messages per participant on both ends`() {
        val container = fakeTestMethodContainer(
            method = alpha,
            invocations = listOf(
                fakeTestInvocation(interactions = setOf(interactionEntry("Request from A to B"), interactionEntry("Response from B to A"))),
                fakeTestInvocation(interactions = setOf(interactionEntry("Ping from A to C"))),
            ),
        )

        IndexMetrics.participantCounts(container) shouldBe mapOf("A" to 3, "B" to 2, "C" to 1)
    }

    @Test
    fun `counts multi word party names`() {
        val container = fakeTestMethodContainer(
            method = alpha,
            invocations = listOf(fakeTestInvocation(interactions = setOf(interactionEntry("Msg from Adoption Service to Credit Bureau")))),
        )

        IndexMetrics.participantCounts(container) shouldBe mapOf("Adoption Service" to 1, "Credit Bureau" to 1)
    }

    @Test
    fun `counts party names containing punctuation`() {
        val container = fakeTestMethodContainer(
            method = alpha,
            invocations = listOf(fakeTestInvocation(interactions = setOf(interactionEntry("Order from web-ui to order.service")))),
        )

        IndexMetrics.participantCounts(container) shouldBe mapOf("web-ui" to 1, "order.service" to 1)
    }

    @Test
    fun `ignores keys that do not name two parties`() {
        val container = fakeTestMethodContainer(method = alpha, invocations = listOf(fakeTestInvocation(interactions = setOf(interactionEntry("stray")))))

        IndexMetrics.participantCounts(container) shouldBe emptyMap()
        IndexMetrics.interactionCount(container) shouldBe 1
    }

    @Test
    fun `counts sentences that start with a then keyword as assertions`() {
        val container = fakeTestMethodContainer(
            method = alpha,
            invocations = listOf(fakeTestInvocation(sentences = listOf(
                sentence(kw("Given"), word("a thing")),
                sentence(kw("When"), word("it runs")),
                sentence(kw("Then"), word("it works")),
                sentence(kw("Then eventually"), word("it settles")),
                sentence(kw("And"), word("more")),
            ))),
        )

        IndexMetrics.assertionCount(container) shouldBe 2
    }

    @Test
    fun `counts sentences containing an expandable`() {
        val container = fakeTestMethodContainer(
            method = alpha,
            invocations = listOf(fakeTestInvocation(sentences = listOf(
                sentence(kw("Then"), expandable("theOrderIsShipped")),
                sentence(kw("And"), word("nothing else")),
            ))),
        )

        IndexMetrics.expandableCount(container) shouldBe 1
    }
}
