package dev.kensa.parse

import dev.kensa.ElementDescriptor
import dev.kensa.sentence.Sentence

data class ParsedMethod(
    val indexInSource: Int,
    val name: String,
    val parameters: MethodParameters,
    val sentences: List<Sentence>,
    val nestedSentences: Map<String, List<Sentence>>,
    val properties: Map<String, ElementDescriptor>,
    val methods: Map<String, ElementDescriptor>
)