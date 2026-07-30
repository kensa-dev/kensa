package dev.kensa.kotest.testsupport.collections

import io.kotest.matchers.be
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.junit.jupiter.api.Test

class MatchersTest {

    @Test
    fun `noneMatching passes on an empty collection`() {
        noneMatching(be("x")).test(emptyList<String>()).passed() shouldBe true
    }

    @Test
    fun `noneMatching passes when no element matches`() {
        noneMatching(be("x")).test(listOf("a", "b")).passed() shouldBe true
    }

    @Test
    fun `noneMatching fails listing the matching elements`() {
        val result = noneMatching(be("x")).test(listOf("a", "x", "x"))
        result.passed() shouldBe false
        result.failureMessage() shouldContain "found 2"
        result.failureMessage() shouldContain "x"
    }

    @Test
    fun `noneMatching negated message reports absence`() {
        noneMatching(be("x")).test(listOf("a")).negatedFailureMessage() shouldContain "at least one"
    }
}
