package dev.kensa.parse

import dev.kensa.parse.Accessor.ValueAccessor.MethodAccessor
import dev.kensa.parse.Accessor.ValueAccessor.PropertyAccessor
import dev.kensa.sentence.Sentence

data class ParsedMethod(
        val name: String,
        val parameters: MethodParameters,
        val sentences: List<Sentence>,
        val nestedSentences: Map<String, List<Sentence>>,
        val properties: Map<String, PropertyAccessor>,
        val methods: Map<String, MethodAccessor>
)