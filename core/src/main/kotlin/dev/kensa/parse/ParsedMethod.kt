package dev.kensa.parse

import dev.kensa.sentence.TemplateSentence

data class ParsedMethod(
    val indexInSource: Int,
    val name: String,
    val parameters: MethodParameters,
    val sentences: List<TemplateSentence>,
    val expandableMethods: Map<String, ParsedExpandableMethod>,
    val properties: Map<String, ElementDescriptor>,
    val methods: Map<String, ElementDescriptor>,
    val parseErrors: List<ParseError> = emptyList()
)

class ParsedExpandableMethod(
    val name: String,
    val parameters: MethodParameters,
    sentencesProvider: () -> List<TemplateSentence>
) {
    private val lazySentences = lazy(LazyThreadSafetyMode.SYNCHRONIZED, sentencesProvider)
    val sentences: List<TemplateSentence> get() = lazySentences.value

    constructor(name: String, parameters: MethodParameters, sentences: List<TemplateSentence>) :
        this(name, parameters, { sentences })
}