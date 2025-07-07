package dev.kensa.parse

import dev.kensa.ElementDescriptor
import dev.kensa.sentence.TemplateSentence

data class ParsedMethod(
    val indexInSource: Int,
    val name: String,
    val parameters: MethodParameters,
    val sentences: List<TemplateSentence>,
    val nestedMethods: Map<String, ParsedNestedMethod>,
    val properties: Map<String, ElementDescriptor>,
    val methods: Map<String, ElementDescriptor>
)

data class ParsedNestedMethod(
    val name: String,
    val parameters: MethodParameters,
    val sentences: List<TemplateSentence>
)