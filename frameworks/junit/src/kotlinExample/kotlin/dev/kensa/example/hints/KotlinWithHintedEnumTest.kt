package dev.kensa.example.hints

import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.RenderedValueWithHint
import dev.kensa.example.KotlinExampleTest
import dev.kensa.example.hints.KotlinEnumWithValue.Value1
import org.junit.jupiter.api.Test

@RenderedValueWithHint(type = KotlinEnumWithValue::class, valueStrategy = UseIdentifierName, valueParam = "value")
class KotlinWithHintedEnumTest : KotlinExampleTest() {

    @Test
    fun passingTest() {
        Value1 of "Value1"
    }

    infix fun KotlinEnumWithValue.of(expected: String) = expected
}