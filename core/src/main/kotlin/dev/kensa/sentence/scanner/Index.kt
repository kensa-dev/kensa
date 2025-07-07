package dev.kensa.sentence.scanner

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.TemplateToken.Type

data class Index(val type: Type, val start: Int, val end: Int, val emphasis: EmphasisDescriptor? = null) {
    fun cancels(other: Index): Boolean = other.start in start until end
}