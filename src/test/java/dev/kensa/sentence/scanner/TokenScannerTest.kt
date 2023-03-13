package dev.kensa.sentence.scanner

import dev.kensa.sentence.Acronym
import dev.kensa.sentence.Dictionary
import dev.kensa.sentence.HighlightedIdentifier
import dev.kensa.sentence.TokenType.Keyword
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class TokenScannerTest {
    private val acronyms = setOf(
        Acronym.of("FTTC", "Fibre To The Cabinet"),
        Acronym.of("FTTP", "Fibre To The Premises"),
        Acronym.of("TT", "Tourist Trophy"),
        Acronym.of("BT", "British Telecom"),
        Acronym.of("FT", "Financial Times"),
        Acronym.of("ONT", "Optical Network Termination")
    )

    @Test
    internal fun recognisesHighlightedIdentifiers() {
        val identifier = "MyIdentifier"

        val (actual, indices) = tokenScannerWith(emptySet(), setOf(HighlightedIdentifier("MyIdentifier"))).scan(identifier)

        actual shouldBe identifier
        transformed(actual, indices) shouldBe listOf(identifier)
    }

    @Test
    fun recognisesKeywordsAtStartOfSentenceOnly() {
        val string = "givenSomethingWasGivenWhenThen"
        val (_, indices) = tokenScannerWith(emptySet()).scan(string)

        indices.filter { index -> index.type === Keyword }
            .forEach { index -> index.start shouldBe 0 }
    }

    @Test
    internal fun normalisesKotlinWheneverToWhen() {
        val string = "wheneverAThingHappens"
        val expected = listOf("when", "A", "Thing", "Happens")
        val (scanned, indices) = tokenScannerWith(emptySet()).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @Test
    fun splitsInternalNumbersToSeparateTokens() {
        val expected = listOf("last", "24", "Hours")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(emptySet()).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @Test
    fun splitsTrailingNumbersToSeparateTokens() {
        val expected = listOf("last", "24")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(emptySet()).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @Test
    fun scansSimpleStringWithNoAcronyms() {
        val expected = listOf("given", "This", "And", "That")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(emptySet()).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @Test
    fun scansStringWithSingleAcronymInMiddle() {
        val expected = listOf("given", "FTTC", "And", "This", "And", "That")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(acronyms).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    // Kensa#6
    @Test
    fun scansStringWithSingleCharacterFollowedByAcronym() {
        val expected = listOf("a", "FTTC", "And", "This", "And", "That")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(acronyms).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @Test
    fun scansStringWithSingleAcronymAtEnd() {
        val expected = listOf("given", "And", "This", "And", "That", "FTTC")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(acronyms).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @Test
    fun scansStringWithMultipleAcronyms() {
        val expected = listOf("given", "And", "FTTP", "This", "And", "That", "FTTC")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(acronyms).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @Test
    fun choosesLongestMatchingAcronym() {
        val expected = listOf("given", "FT", "And", "FTTP", "This", "And", "That", "FTTC")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(acronyms).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @ParameterizedTest
    @MethodSource("mixedCaseExamples")
    fun scansAcronymsCorrectlyWhenMixedCase(expected: List<String>) {
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(acronyms).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @Test
    fun scansStringWithAcronymSpanningCamelWords() {
        val expected = listOf("a", "Notification", "Type", "Of")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(acronyms).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    @Test
    fun scansStringWithAcronymSpanningCamelWords_afterInitialCap() {
        val expected = listOf("a", "Contact")
        val string = expected.joinToString("")
        val (scanned, indices) = tokenScannerWith(acronyms).scan(string)

        transformed(scanned, indices) shouldBe expected
    }

    private fun transformed(string: String, indices: Indices): List<String> =
        indices.map { index -> string.substring(index.start, index.end) }.toList()

    private fun tokenScannerWith(acronyms: Set<Acronym>, highlightedIdentifiers: Set<HighlightedIdentifier> = emptySet()): TokenScanner = TokenScanner(
        Dictionary().apply {
            putAcronyms(acronyms)
            putHighlightedIdentifiers(highlightedIdentifiers)
        }
    )

    companion object {
        @JvmStatic
        fun mixedCaseExamples(): Stream<List<String>> {
            return Stream.of(
                listOf("FTTC"),
                listOf("Fttc"),
                listOf("Ftt", "C"),
                listOf("Ft", "Tc"),
                listOf("Ft", "TC"),
                listOf("F", "Ttc"),
                listOf("Ft", "Tc"),
                listOf("fttc")
            )
        }
    }
}