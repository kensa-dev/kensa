package dev.kensa.sentence

import dev.kensa.sentence.Acronym.Companion.of
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class DictionaryTest {
    private lateinit var dictionary: Dictionary

    @BeforeEach
    fun setUp() {
        dictionary = Dictionary()
    }

    @AfterEach
    fun tearDown() {
        dictionary.clearAcronyms()
    }

    @Test
    fun canStreamExistingAcronyms() {
        val acronyms = arrayOf(
                of("foo", "means foo"),
                of("boo", "means boo"),
                of("moo", "means moo")
        )
        dictionary.putAcronyms(*acronyms)
        dictionary.acronyms shouldContainAll acronyms.toList()
    }

    @Test
    fun canPutKeywords() {
        dictionary.putKeyword("foo")
        dictionary.putKeywords("boo", "moo")
        dictionary.keywords shouldContainAll listOf("foo", "boo", "moo").map { Keyword(it) }
    }

    @Test
    fun canClearAcronyms() {
        dictionary.putAcronyms(of("foo", "foo"))
        dictionary.clearAcronyms()
        dictionary.acronyms.shouldBeEmpty()
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun throwsOnAttemptToAddInvalidKeyword(keyword: String) {
        shouldThrowExactly<IllegalArgumentException> { dictionary.putKeyword(keyword) }
        shouldThrowExactly<IllegalArgumentException> { dictionary.putKeywords("foo", keyword) }
    }

    companion object {
        @JvmStatic
        fun arguments(): Stream<Arguments> {
            return Stream.of(
                    Arguments.of(""),
                    Arguments.of("   "),
                    Arguments.of("x"),
                    Arguments.of("xx")
            )
        }
    }
}