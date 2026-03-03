package dev.kensa.example.hints

import dev.kensa.RenderedHintStrategy.HintFromProperty
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.RenderedValueWithHint
import dev.kensa.Sources
import dev.kensa.example.KotlinExampleTest
import dev.kensa.example.hints.KotlinFields.aString
import dev.kensa.example.hints.KotlinFields.anInteger
import org.junit.jupiter.api.Test

@RenderedValueWithHint(type = KotlinFieldWithPathHint::class, valueStrategy = UseIdentifierName, hintParam = "path", hintStrategy = HintFromProperty)
@Sources(KotlinFields::class)
class KotlinWithHintedFieldsInsideObjectTest : KotlinExampleTest() {

    @Test
    fun passingTest() {
        aString of "expected"
        anInteger of 10
    }

}
