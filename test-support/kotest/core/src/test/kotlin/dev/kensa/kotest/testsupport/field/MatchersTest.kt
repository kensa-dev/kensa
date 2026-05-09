package dev.kensa.kotest.testsupport.field

import io.kotest.matchers.be
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.jupiter.api.Test

class MatchersTest {

    @Test
    fun `nullableExtractingMatcher passes when underlying matcher passes`() {
        val matcher = nullableExtractingMatcher<String, String>("Foo", { it.uppercase() }, be("ABC"))
        matcher.test("abc").passed() shouldBe true
    }

    @Test
    fun `nullableExtractingMatcher fails with name-prefixed message`() {
        val matcher = nullableExtractingMatcher<String, String>("Foo", { it.uppercase() }, be("ZZZ"))
        val result = matcher.test("abc")
        result.passed() shouldBe false
        result.failureMessage() shouldStartWith "Foo:"
    }

    @Test
    fun `nullableExtractingMatcher passes null subject straight to matcher`() {
        val matcher = nullableExtractingMatcher<String, String>("Foo", { it.uppercase() }, be(null))
        matcher.test(null).passed() shouldBe true
    }

    @Test
    fun `nullableExtractingMatcher uses negated message on negated invocation`() {
        val matcher = nullableExtractingMatcher<String, String>("Foo", { it.uppercase() }, be("ABC"))
        matcher.test("abc").negatedFailureMessage() shouldStartWith "Foo:"
    }
}
