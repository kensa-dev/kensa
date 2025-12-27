package dev.kensa.example

import dev.kensa.RenderedHintStrategy.HintFromProperty
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.RenderedValueWithHint

@RenderedValueWithHint(type = Field::class, valueStrategy = UseIdentifierName, hintParam = "path", hintStrategy = HintFromProperty)
class KotlinWithHintedFields {

    fun simpleTest() {
        aStringField of "expected"
        anIntegerField of 10
    }

    val aStringField = Field<String>("/path/to/string")
    val anIntegerField = Field<Int>("/path/to/integer")
}

class Field<T>(val path: String) {
    infix fun of(value: T) = this
}

