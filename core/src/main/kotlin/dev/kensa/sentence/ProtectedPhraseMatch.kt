package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor

data class ProtectedPhraseMatch(
    val start: Int,
    val end: Int,
    val emphasisDescriptor: EmphasisDescriptor
)