package dev.kensa.sentence

import dev.kensa.sentence.Acronym.Companion.of
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
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
        assertThat(dictionary.acronyms).containsAll(acronyms.toList())
    }

    @Test
    fun canPutKeywords() {
        dictionary.putKeyword("foo")
        dictionary.putKeywords("boo", "moo")
        assertThat(dictionary.keywords).containsAll(listOf("foo", "boo", "moo").map { Keyword(it) })
    }

    @Test
    fun canClearAcronyms() {
        dictionary.putAcronyms(of("foo", "foo"))
        dictionary.clearAcronyms()
        assertThat(dictionary.acronyms).isEmpty()
    }

    @ParameterizedTest
    @MethodSource("arguments")
    fun throwsOnAttemptToAddInvalidKeyword(keyword: String) {
        assertThatThrownBy { dictionary.putKeyword(keyword) }
                .isInstanceOf(IllegalArgumentException::class.java)
        assertThatThrownBy { dictionary.putKeywords("foo", keyword) }
                .isInstanceOf(IllegalArgumentException::class.java)
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