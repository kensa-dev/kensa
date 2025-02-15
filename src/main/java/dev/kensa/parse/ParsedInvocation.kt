package dev.kensa.parse

import dev.kensa.sentence.Sentence
import dev.kensa.util.NamedValue

class ParsedInvocation(val name: String, val namedParameterValues: Collection<NamedValue>, val sentences: List<Sentence>, val highlightedValues: Set<NamedValue>, val parameterizedTestDescription: String?)