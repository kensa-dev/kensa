package dev.kensa.output.unfurl

import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.RenderedToken
import dev.kensa.sentence.RenderedToken.RenderedExpandableToken
import dev.kensa.sentence.RenderedToken.RenderedValueToken
import dev.kensa.state.TestState.Failed
import dev.kensa.state.TestState.NotExecuted
import dev.kensa.state.TestState.Passed
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldHaveMaxLength
import org.junit.jupiter.api.Test

class UnfurlDescriptionTest {

    @Test
    fun `opens with the state and the class`() {
        unfurlDescription(Passed, "Order Test", emptyList()) shouldBe "Passed · Order Test"
        unfurlDescription(Failed, "Order Test", emptyList()) shouldBe "Failed · Order Test"
        unfurlDescription(NotExecuted, "Order Test", emptyList()) shouldBe "Not Executed · Order Test"
    }

    @Test
    fun `each sentence follows on its own line, tokens separated by a space`() {
        val sentences = listOf(
            sentence(token("Given"), token("an"), token("order"), token("for"), token("2 items")),
            sentence(token("Then"), token("the"), token("order"), token("is"), token("accepted")),
        )

        unfurlDescription(Passed, "Order Test", sentences) shouldBe
            "Passed · Order Test\nGiven an order for 2 items\nThen the order is accepted"
    }

    @Test
    fun `line breaks and indents inside a sentence collapse to a space`() {
        val sentences = listOf(sentence(token("Given"), token(""), token(""), token("an"), token("order")))

        unfurlDescription(Passed, "Order Test", sentences) shouldBe "Passed · Order Test\nGiven an order"
    }

    @Test
    fun `an expandable contributes its own label only`() {
        val expandable = RenderedExpandableToken(
            value = "the order is placed",
            cssClasses = setOf("ex"),
            name = "theOrderIsPlaced",
            parameterTokens = listOf(token("with"), token("42")),
            expandableTokens = listOf(listOf(token("hidden"), token("detail"))),
        )

        unfurlDescription(Passed, "Order Test", listOf(sentence(token("When"), expandable))) shouldBe
            "Passed · Order Test\nWhen the order is placed"
    }

    @Test
    fun `is cut at three hundred characters on a word boundary`() {
        val words = (1..80).map { "word$it" }
        val sentences = listOf(sentence(*words.map { token(it) }.toTypedArray()))
        val full = "Passed · Order Test\n" + words.joinToString(" ")

        val description = unfurlDescription(Passed, "Order Test", sentences)

        description shouldHaveMaxLength 300
        description shouldEndWith "…"
        val kept = description.removeSuffix("…")
        full shouldStartWith kept
        full[kept.length] shouldBe ' '
    }

    @Test
    fun `three hundred characters exactly are left alone`() {
        val text = "x".repeat(300 - "Passed · Order Test\n".length)

        unfurlDescription(Passed, "Order Test", listOf(sentence(token(text)))) shouldBe "Passed · Order Test\n$text"
    }

    private fun sentence(vararg tokens: RenderedToken) = RenderedSentence(tokens.toList(), lineNumber = 1)

    private fun token(value: String) = RenderedValueToken(value = value, cssClasses = setOf("wd"))
}
