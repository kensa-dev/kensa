package dev.kensa.sentence.scanner

internal class TokenScannerTest {
//    @Test
//    fun recognisesKeywordsAtStartOfSentenceOnly() {
//        val string = "givenSomethingWasGivenWhenThen"
//        val indices = tokenScannerWith(emptySet()).scan(string)
//
//        indices.filter { index -> index.type === Keyword }
//                .forEach { index -> assertThat(index.start).isEqualTo(0) }
//    }
//
//    @Test
//    fun splitsInternalNumbersToSeparateTokens() {
//        val expected = listOf("last", "24", "Hours")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(emptySet()).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @Test
//    fun splitsTrailingNumbersToSeparateTokens() {
//        val expected = listOf("last", "24")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(emptySet()).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @Test
//    fun scansSimpleStringWithNoAcronyms() {
//        val expected = listOf("given", "This", "And", "That")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(emptySet()).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @Test
//    fun scansStringWithSingleAcronymInMiddle() {
//        val acronyms = setOf("FTTC", "FTTP", "TT", "BT")
//        val expected = listOf("given", "FTTC", "And", "This", "And", "That")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(acronyms).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    // Kensa#6
//    @Test
//    fun scansStringWithSingleCharacterFollowedByAcronym() {
//        val acronym = setOf("FTTC")
//        val expected = listOf("a", "FTTC", "And", "This", "And", "That")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(acronym).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @Test
//    fun scansStringWithSingleAcronymAtEnd() {
//        val acronym = setOf("FTTC", "FTTP", "TT", "BT")
//        val expected = listOf("given", "And", "This", "And", "That", "FTTC")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(acronym).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @Test
//    fun scansStringWithMultipleAcronyms() {
//        val acronym = setOf("FTTC", "FTTP", "TT", "BT")
//        val expected = listOf("given", "And", "FTTP", "This", "And", "That", "FTTC")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(acronym).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @Test
//    fun choosesLongestMatchingAcronym() {
//        val acronyms = setOf("FTTC", "FTTP", "FT", "BT")
//        val expected = listOf("given", "FT", "And", "FTTP", "This", "And", "That", "FTTC")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(acronyms).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @ParameterizedTest
//    @MethodSource("mixedCaseExamples")
//    fun scansAcronymsCorrectlyWhenMixedCase(expected: List<String>) {
//        val acronyms = setOf("FTTC")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(acronyms).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @Test
//    fun scansStringWithAcronymSpanningCamelWords() {
//        val acronyms = setOf("ONT")
//        val expected = listOf("a", "Notification", "Type", "Of")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(acronyms).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @Test
//    fun scansStringWithAcronymSpanningCamelWords_afterInitialCap() {
//        val acronyms = setOf("ONT")
//        val expected = listOf("a", "Contact")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(acronyms).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//    }
//
//    @Test
//    fun scansStringWithHighlightedWords() {
//        val expected = listOf("An", "Important", "Priority", "Thing", "Notification")
//        val string = expected.joinToString("")
//        val indices = tokenScannerWith(emptySet()).scan(string)
//
//        assertThat(transformed(indices, string)).isEqualTo(expected)
//        assertThat(indices.map { it.type }).containsExactly(Word, HighlightedWord, HighlightedWord, Word, Word)
//    }
//
//    private fun transformed(indices: Indices, string: String): List<String> =
//            indices.map { index -> string.substring(index.start, index.end) }.toList()
//
//    private fun tokenScannerWith(acronyms: Set<String>): TokenScanner = TokenScanner(setOf("Important", "Priority", "No"), setOf("Given", "When", "Then"), acronyms)
//
//    companion object {
//        @JvmStatic
//        private fun mixedCaseExamples(): Stream<List<String>> {
//            return Stream.of(
//                    listOf("FTTC"),
//                    listOf("Fttc"),
//                    listOf("Ftt", "C"),
//                    listOf("Ft", "Tc"),
//                    listOf("Ft", "TC"),
//                    listOf("F", "Ttc"),
//                    listOf("Ft", "Tc"),
//                    listOf("fttc")
//            )
//        }
//    }
}