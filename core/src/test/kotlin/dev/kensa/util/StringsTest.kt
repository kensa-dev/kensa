package dev.kensa.util

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

internal class StringsTest {
    @Test
    internal fun `can un-camel`() {
        "".unCamel() shouldBe ""
        " ".unCamel() shouldBe " "
        "camelCasePhrase".unCamel() shouldBe "Camel Case Phrase"
        " camelCasePhrase ".unCamel() shouldBe "Camel Case Phrase"
        "CamelCasePhrase".unCamel() shouldBe "Camel Case Phrase"
        "CamelCasePhrase_WithSuffix".unCamel() shouldBe "Camel Case Phrase With Suffix"
        "\$CamelCasePhrase".unCamel() shouldBe "Camel Case Phrase"
        "_CamelCasePhrase".unCamel() shouldBe "Camel Case Phrase"
        "aCamelCasePhrase".unCamel() shouldBe "A Camel Case Phrase"
        "CamelCasePhraseWithAnItem".unCamel() shouldBe "Camel Case Phrase With An Item"
        "CamelCaseFTTCPhrase".unCamel() shouldBe "Camel Case FTTC Phrase"
        "CamelCasePhraseFTTC".unCamel() shouldBe "Camel Case Phrase FTTC"
        "FTTCCamelCasePhrase".unCamel() shouldBe "FTTC Camel Case Phrase"
        "FTTCCamelCasePhrase9000".unCamel() shouldBe "FTTC Camel Case Phrase 9000"
        "FTTCCamel9000CasePhrase".unCamel() shouldBe "FTTC Camel 9000 Case Phrase"
        "9000FTTCCamelCasePhrase".unCamel() shouldBe "9000 FTTC Camel Case Phrase"
        "9000 FTTC Camel Case Phrase".unCamel() shouldBe "9000 FTTC Camel Case Phrase"
    }

    @Test
    internal fun `can un-camel and separate with separator`() {
        "".unCamelToSeparated() shouldBe ""
        " ".unCamelToSeparated() shouldBe " "
        "camelCasePhrase".unCamelToSeparated() shouldBe "camel-case-phrase"
        " camelCasePhrase ".unCamelToSeparated() shouldBe "camel-case-phrase"
        "CamelCasePhrase".unCamelToSeparated() shouldBe "camel-case-phrase"

        "camelCasePhrase".unCamelToSeparated(".") shouldBe "camel.case.phrase"
        " camelCasePhrase ".unCamelToSeparated(".") shouldBe "camel.case.phrase"
        "CamelCasePhrase".unCamelToSeparated(".") shouldBe "camel.case.phrase"
    }
}