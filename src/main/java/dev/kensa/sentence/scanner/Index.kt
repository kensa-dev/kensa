package dev.kensa.sentence.scanner

import dev.kensa.parse.EmphasisDescriptor
import dev.kensa.sentence.TokenType

data class Index(val type: TokenType, val start: Int, val end: Int, val emphasisDescriptor: EmphasisDescriptor? = null) {
    fun cancels(other: Index): Boolean = other.start in start until end
}