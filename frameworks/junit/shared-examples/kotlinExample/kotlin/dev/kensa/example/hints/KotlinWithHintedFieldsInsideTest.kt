package dev.kensa.example.hints

import dev.kensa.RenderedHintStrategy.HintFromProperty
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.RenderedValueWithHint
import dev.kensa.example.KotlinExampleTest
import org.junit.jupiter.api.Test

@RenderedValueWithHint(type = KotlinFieldWithPathHint::class, valueStrategy = UseIdentifierName, hintParam = "path", hintStrategy = HintFromProperty)
class KotlinWithHintedFieldsInsideTest : KotlinExampleTest() {

    @Test
    fun passingTest() {
        aString of "expected"
        anInteger of 10
    }

    private val aString = KotlinFieldWithPathHint<String>("/path/To/String/Field")
    private val anInteger = KotlinFieldWithPathHint<Int>("/path/To/Integer/Field")

}
