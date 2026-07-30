package dev.kensa.hamkrest.testsupport.collections

import com.natpryce.hamkrest.MatchResult
import com.natpryce.hamkrest.equalTo
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class MatchersTest {

    @Test
    fun `noneMatching passes on an empty collection`() {
        noneMatching(equalTo("x"))(emptyList<String>()) shouldBe MatchResult.Match
    }

    @Test
    fun `noneMatching passes when no element matches`() {
        noneMatching(equalTo("x"))(listOf("a", "b")) shouldBe MatchResult.Match
    }

    @Test
    fun `noneMatching fails listing the matching elements`() {
        val result = noneMatching(equalTo("x"))(listOf("a", "x", "x"))
        val mismatch = result.shouldBeInstanceOf<MatchResult.Mismatch>()
        mismatch.description shouldContain "found 2"
        mismatch.description shouldContain "x"
    }

    @Test
    fun `noneMatching description embeds the inner matcher's description`() {
        noneMatching(equalTo("x")).description shouldContain equalTo("x").description
    }
}
