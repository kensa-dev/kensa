package dev.kensa.sentence.scanner

import dev.kensa.sentence.TokenType

data class Index internal constructor(val type: TokenType, val start: Int, val end: Int) {
    fun cancels(other: Index): Boolean = other.start in start until end
}