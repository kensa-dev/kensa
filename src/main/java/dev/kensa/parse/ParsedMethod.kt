package dev.kensa.parse

import dev.kensa.sentence.Sentence

data class ParsedMethod(
        val name: String,
        val parameters: MethodParameters,
        val sentences: List<Sentence>,
        val nestedSentences: Map<String, List<Sentence>>,
        val fields: Map<String, PropertyDescriptor>
)