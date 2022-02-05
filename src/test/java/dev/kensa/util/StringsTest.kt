package dev.kensa.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StringsTest {
    @Test
    internal fun `can un-camel`() {
        assertThat("".unCamel()).isEqualTo("")
        assertThat(" ".unCamel()).isEqualTo(" ")
        assertThat("camelCasePhrase".unCamel()).isEqualTo("Camel Case Phrase")
        assertThat(" camelCasePhrase ".unCamel()).isEqualTo("Camel Case Phrase")
        assertThat("CamelCasePhrase".unCamel()).isEqualTo("Camel Case Phrase")
        assertThat("CamelCasePhrase_WithSuffix".unCamel()).isEqualTo("Camel Case Phrase With Suffix")
        assertThat("\$CamelCasePhrase".unCamel()).isEqualTo("Camel Case Phrase")
        assertThat("_CamelCasePhrase".unCamel()).isEqualTo("Camel Case Phrase")
        assertThat("aCamelCasePhrase".unCamel()).isEqualTo("A Camel Case Phrase")
        assertThat("CamelCasePhraseWithAnItem".unCamel()).isEqualTo("Camel Case Phrase With An Item")
        assertThat("CamelCaseFTTCPhrase".unCamel()).isEqualTo("Camel Case FTTC Phrase")
        assertThat("CamelCasePhraseFTTC".unCamel()).isEqualTo("Camel Case Phrase FTTC")
        assertThat("FTTCCamelCasePhrase".unCamel()).isEqualTo("FTTC Camel Case Phrase")
        assertThat("FTTCCamelCasePhrase9000".unCamel()).isEqualTo("FTTC Camel Case Phrase 9000")
        assertThat("FTTCCamel9000CasePhrase".unCamel()).isEqualTo("FTTC Camel 9000 Case Phrase")
        assertThat("9000FTTCCamelCasePhrase".unCamel()).isEqualTo("9000 FTTC Camel Case Phrase")
    }

    @Test
    internal fun `can un-camel and separate with separator`() {
        assertThat("".unCamelToSeparated()).isEqualTo("")
        assertThat(" ".unCamelToSeparated()).isEqualTo(" ")
        assertThat("camelCasePhrase".unCamelToSeparated()).isEqualTo("camel-case-phrase")
        assertThat(" camelCasePhrase ".unCamelToSeparated()).isEqualTo("camel-case-phrase")
        assertThat("CamelCasePhrase".unCamelToSeparated()).isEqualTo("camel-case-phrase")

        assertThat("camelCasePhrase".unCamelToSeparated(".")).isEqualTo("camel.case.phrase")
        assertThat(" camelCasePhrase ".unCamelToSeparated(".")).isEqualTo("camel.case.phrase")
        assertThat("CamelCasePhrase".unCamelToSeparated(".")).isEqualTo("camel.case.phrase")
    }
}