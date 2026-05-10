package dev.kensa.hamkrest.testsupport.field

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.Matcher
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.jupiter.api.Test

class PhrasesTest {

    @Test
    fun `with composes two matchers - both must pass`() {
        val combined: Matcher<String?> = startsWithA() with endsWithC()
        combined.invoke("abc") shouldBe MatchResult.Match
        combined.invoke("zzc").shouldBeInstanceOf<MatchResult.Mismatch>()
    }

    @Test
    fun `thatHas with single matcher returns it unchanged`() {
        val matcher = startsWithA()
        thatHas(matcher) shouldBeSameInstanceAs matcher
    }

    @Test
    fun `thatHas with vararg requires all matchers to pass`() {
        val combined = thatHas(startsWithA(), endsWithC())
        combined.invoke("abc") shouldBe MatchResult.Match
        combined.invoke("aaaa").shouldBeInstanceOf<MatchResult.Mismatch>()
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

    private fun startsWithA(): Matcher<String?> = object : Matcher<String?> {
        override val description = "starts with a"
        override fun invoke(actual: String?) = if (actual?.startsWith("a") == true) MatchResult.Match else MatchResult.Mismatch("did not start with a")
    }

    private fun endsWithC(): Matcher<String?> = object : Matcher<String?> {
        override val description = "ends with c"
        override fun invoke(actual: String?) = if (actual?.endsWith("c") == true) MatchResult.Match else MatchResult.Mismatch("did not end with c")
    }

    private fun <T> nonNullMatcher(): Matcher<T?> = object : Matcher<T?> {
        override val description = "is not null"
        override fun invoke(actual: T?) = if (actual != null) MatchResult.Match else MatchResult.Mismatch("was null")
    }
}
