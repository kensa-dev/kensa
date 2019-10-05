package dev.kensa.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StringsTest {

    @Test
    void canUncamel() {
        assertThat(Strings.unCamel(null)).isNull();
        assertThat(Strings.unCamel("")).isEqualTo("");
        assertThat(Strings.unCamel(" ")).isEqualTo(" ");

        assertThat(Strings.unCamel("camelCasePhrase")).isEqualTo("Camel Case Phrase");
        assertThat(Strings.unCamel(" camelCasePhrase ")).isEqualTo("Camel Case Phrase");
        assertThat(Strings.unCamel("CamelCasePhrase")).isEqualTo("Camel Case Phrase");
        assertThat(Strings.unCamel("CamelCasePhrase_WithSuffix")).isEqualTo("Camel Case Phrase With Suffix");
        assertThat(Strings.unCamel("$CamelCasePhrase")).isEqualTo("Camel Case Phrase");
        assertThat(Strings.unCamel("_CamelCasePhrase")).isEqualTo("Camel Case Phrase");
        assertThat(Strings.unCamel("aCamelCasePhrase")).isEqualTo("A Camel Case Phrase");
        assertThat(Strings.unCamel("CamelCasePhraseWithAnItem")).isEqualTo("Camel Case Phrase With An Item");

        assertThat(Strings.unCamel("CamelCaseFTTCPhrase")).isEqualTo("Camel Case FTTC Phrase");
        assertThat(Strings.unCamel("CamelCasePhraseFTTC")).isEqualTo("Camel Case Phrase FTTC");
        assertThat(Strings.unCamel("FTTCCamelCasePhrase")).isEqualTo("FTTC Camel Case Phrase");
        assertThat(Strings.unCamel("FTTCCamelCasePhrase9000")).isEqualTo("FTTC Camel Case Phrase 9000");
        assertThat(Strings.unCamel("FTTCCamel9000CasePhrase")).isEqualTo("FTTC Camel 9000 Case Phrase");
        assertThat(Strings.unCamel("9000FTTCCamelCasePhrase")).isEqualTo("9000 FTTC Camel Case Phrase");
    }

    @Test
    void canIdentifyNullOrBlankStrings() {
        assertThat(Strings.isBlank("foo")).isFalse();
        assertThat(Strings.isNotBlank("foo")).isTrue();
        assertThat(Strings.isBlank(" ")).isTrue();
        assertThat(Strings.isNotBlank(" ")).isFalse();
        assertThat(Strings.isBlank(null)).isTrue();
        assertThat(Strings.isNotBlank(null)).isFalse();
        assertThat(Strings.isBlank("")).isTrue();
        assertThat(Strings.isNotBlank("")).isFalse();
    }

}