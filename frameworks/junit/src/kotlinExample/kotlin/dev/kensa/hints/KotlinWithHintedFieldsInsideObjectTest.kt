package dev.kensa.hints

import dev.kensa.RenderedHintStrategy.HintFromProperty
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.RenderedValueWithHint
import dev.kensa.Sources
import dev.kensa.hints.KotlinFields.aString
import dev.kensa.hints.KotlinFields.anInteger
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.Test

@RenderedValueWithHint(type = KotlinFieldWithPathHint::class, valueStrategy = UseIdentifierName, hintParam = "path", hintStrategy = HintFromProperty)
@Sources(KotlinFields::class)
class KotlinWithHintedFieldsInsideObjectTest : KensaTest {

    @Test
    fun passingTest() {
        aString of "expected"
        anInteger of 10
    }

}
