package dev.kensa.example

import dev.kensa.RenderedValue

open class SourceField(val path: String)
class TextSourceField(path: String) : SourceField(path)

sealed class SealedSourceFields(private val prefix: String) {
    val aSealedField = TextSourceField("$prefix/field")

    @RenderedValue
    val aSealedValue = "$prefix value"

    @RenderedValue
    fun aSealedMethod() = "$prefix method"

    data object A : SealedSourceFields("a")
    data object B : SealedSourceFields("b")
}
