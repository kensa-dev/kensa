package dev.kensa.hamkrest.testsupport.field

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.equalTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class MatchersTest {

    @Test
    fun `extractingMatcher passes when underlying matcher passes`() {
        val matcher = extractingMatcher<String, String>("Foo", { it.uppercase() }, equalTo("ABC"))
        matcher.invoke("abc") shouldBe MatchResult.Match
    }

    @Test
    fun `extractingMatcher fails with name-prefixed description`() {
        val matcher = extractingMatcher<String, String>("Foo", { it.uppercase() }, equalTo("ZZZ"))
        matcher.invoke("abc").shouldBeInstanceOf<MatchResult.Mismatch>()
        matcher.description shouldStartWith "Foo:"
        matcher.negatedDescription shouldStartWith "Foo:"
    }

    @Test
    fun `extractingMatcher passes null subject straight to matcher`() {
        val matcher = extractingMatcher<String, String>("Foo", { it.uppercase() }, equalTo(null))
        matcher.invoke(null) shouldBe MatchResult.Match
    }
}
