package dev.kensa.example

import dev.kensa.RenderedHintStrategy.HintFromProperty
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.RenderedValueWithHint
import dev.kensa.Sources

@RenderedValueWithHint(type = SourceField::class, valueStrategy = UseIdentifierName, hintParam = "path", hintStrategy = HintFromProperty)
@Sources(SealedSourceFields.A::class, SealedSourceFields.B::class)
class KotlinWithTwoObjectSources {
    fun simpleTest() {
        with(SealedSourceFields.B) { aSealedField }
    }
}
