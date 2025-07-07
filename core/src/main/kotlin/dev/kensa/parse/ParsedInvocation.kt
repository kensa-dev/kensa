package dev.kensa.parse

import dev.kensa.sentence.RenderedSentence
import dev.kensa.util.NamedValue

class ParsedInvocation(val indexInSource: Int, val name: String, val namedParameterValues: Collection<NamedValue>, val sentences: List<RenderedSentence>, val highlightedValues: Set<NamedValue>, val parameterizedTestDescription: String?)