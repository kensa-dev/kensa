package dev.kensa.state

import dev.kensa.parse.ParsedTestInvocation
import dev.kensa.render.diagram.SequenceDiagram
import dev.kensa.sentence.Acronym
import dev.kensa.sentence.Sentence
import dev.kensa.state.TestState.Failed
import dev.kensa.state.TestState.Passed
import dev.kensa.util.KensaMap
import dev.kensa.util.NamedValue
import java.time.Duration

class TestInvocation(
        val elapsed: Duration,
        val acronyms: Set<Acronym>,
        val executionException: Throwable?,
        val sequenceDiagram: SequenceDiagram?,
        parsedTestInvocation: ParsedTestInvocation,
        interactions: CapturedInteractions,
        givens: Givens
) {
    val sentences: List<Sentence> = parsedTestInvocation.sentences
    val parameters: Collection<NamedValue> = parsedTestInvocation.namedParameterValues
    val highlightedValues: Collection<NamedValue> = parsedTestInvocation.highlightedValues
    val state: TestState

    private val _givens = givens
    val givens: Set<KensaMap.Entry>
        get() = _givens.entrySet()

    private val _interactions = interactions
    val interactions: Set<KensaMap.Entry>
        get() = _interactions.entrySet()

    init {
        _givens.putNamedValues(highlightedValues)
        state = if (executionException == null) Passed else Failed
    }
}