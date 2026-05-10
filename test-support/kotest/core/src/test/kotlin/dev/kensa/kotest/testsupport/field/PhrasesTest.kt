package dev.kensa.kotest.testsupport.field

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Test

class PhrasesTest {

    @Test
    fun `with composes two matchers - both must pass`() {
        val combined: Matcher<String> = startsWithA() with endsWithC()
        combined.test("abc").passed() shouldBe true
        combined.test("zzc").passed() shouldBe false
    }

    @Test
    fun `thatHas with single matcher returns it unchanged`() {
        val matcher = startsWithA()
        thatHas(matcher) shouldBeSameInstanceAs matcher
    }

    @Test
    fun `thatHas with vararg requires all matchers to pass`() {
        val combined = thatHas(startsWithA(), endsWithC())
        combined.test("abc").passed() shouldBe true
        combined.test("aaaa").passed() shouldBe false
    }

    @Test
    fun `thatIs returns matcher unchanged`() {
        val matcher = startsWithA()
        thatIs(matcher) shouldBeSameInstanceAs matcher
    }

    @Test
    fun `shouldHaveAll passes silently when all matchers match`() {
        "abc".shouldHaveAll(startsWithA(), endsWithC())
    }

    @Test
    fun `shouldHaveAll throws AssertionError when any matcher fails`() {
        shouldThrow<AssertionError> {
            "abz".shouldHaveAll(startsWithA(), endsWithC())
        }
    }

    @Test
    fun `shouldHaveAll on null subject delegates to nullable-aware matchers`() {
        val nonNull = nonNullMatcher<String>()
        shouldThrow<AssertionError> {
            (null as String?).shouldHaveAll(nonNull)
        }
    }

    private fun startsWithA(): Matcher<String?> = Matcher { value ->
        MatcherResult(value?.startsWith("a") == true, { "expected start with a" }, { "expected not to start with a" })
    }

    private fun endsWithC(): Matcher<String?> = Matcher { value ->
        MatcherResult(value?.endsWith("c") == true, { "expected end with c" }, { "expected not to end with c" })
    }

    private fun <T> nonNullMatcher(): Matcher<T?> = Matcher { value ->
        MatcherResult(value != null, { "expected non-null" }, { "expected null" })
    }
}
