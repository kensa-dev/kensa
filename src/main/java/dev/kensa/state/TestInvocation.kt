package dev.kensa.state

import dev.kensa.parse.HighlightDescriptors
import dev.kensa.parse.ParsedInvocation
import dev.kensa.render.diagram.SequenceDiagram
import dev.kensa.sentence.Sentence
import dev.kensa.state.TestState.Failed
import dev.kensa.state.TestState.Passed
import dev.kensa.util.KensaMap
import dev.kensa.util.NamedValue
import java.time.Duration

class TestInvocation(
    val elapsed: Duration,
    val displayName: String,
    val executionException: Throwable?,
    val sequenceDiagram: SequenceDiagram?,
    parsedInvocation: ParsedInvocation,
    interactions: CapturedInteractions,
    givens: Givens
) {
    val sentences: List<Sentence> = parsedInvocation.sentences
    val parameters: Collection<NamedValue> = parsedInvocation.namedParameterValues
    val parameterizedTestDescription: String? = parsedInvocation.parameterizedTestDescription
    val highlightDescriptors: HighlightDescriptors = parsedInvocation.highlightDescriptors
    val state: TestState

    private val _givens = givens
    val givens: Set<KensaMap.Entry>
        get() = _givens.entrySet()

    private val _interactions = interactions
    val interactions: Set<KensaMap.Entry>
        get() = _interactions.entrySet()

    init {
        _givens.putNamedValues(parsedInvocation.defaultGivens) 
        state = if (executionException == null) Passed else Failed
    }
}