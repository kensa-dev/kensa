package dev.kensa.sentence.scanner

import dev.kensa.sentence.TemplateToken.Type

internal data class Index(val type: Type, val start: Int, val end: Int) {
    fun cancels(other: Index): Boolean = other.start in start until end
}
