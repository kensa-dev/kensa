package dev.kensa.util

import dev.kensa.util.Strings.unCamel
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class StringsTest {
    @Test
    fun canUnCamel() {
        assertThat(unCamel("")).isEqualTo("")
        assertThat(unCamel(" ")).isEqualTo(" ")
        assertThat(unCamel("camelCasePhrase")).isEqualTo("Camel Case Phrase")
        assertThat(unCamel(" camelCasePhrase ")).isEqualTo("Camel Case Phrase")
        assertThat(unCamel("CamelCasePhrase")).isEqualTo("Camel Case Phrase")
        assertThat(unCamel("CamelCasePhrase_WithSuffix")).isEqualTo("Camel Case Phrase With Suffix")
        assertThat(unCamel("\$CamelCasePhrase")).isEqualTo("Camel Case Phrase")
        assertThat(unCamel("_CamelCasePhrase")).isEqualTo("Camel Case Phrase")
        assertThat(unCamel("aCamelCasePhrase")).isEqualTo("A Camel Case Phrase")
        assertThat(unCamel("CamelCasePhraseWithAnItem")).isEqualTo("Camel Case Phrase With An Item")
        assertThat(unCamel("CamelCaseFTTCPhrase")).isEqualTo("Camel Case FTTC Phrase")
        assertThat(unCamel("CamelCasePhraseFTTC")).isEqualTo("Camel Case Phrase FTTC")
        assertThat(unCamel("FTTCCamelCasePhrase")).isEqualTo("FTTC Camel Case Phrase")
        assertThat(unCamel("FTTCCamelCasePhrase9000")).isEqualTo("FTTC Camel Case Phrase 9000")
        assertThat(unCamel("FTTCCamel9000CasePhrase")).isEqualTo("FTTC Camel 9000 Case Phrase")
        assertThat(unCamel("9000FTTCCamelCasePhrase")).isEqualTo("9000 FTTC Camel Case Phrase")
    }
}