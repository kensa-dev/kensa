package dev.kensa.parse

import org.antlr.v4.runtime.Parser
import org.antlr.v4.runtime.atn.ATN
import org.antlr.v4.runtime.atn.ParserATNSimulator
import org.antlr.v4.runtime.atn.PredictionContextCache
import org.antlr.v4.runtime.dfa.DFA

// A generated parser keeps its DFA and prediction contexts in static fields that only ever grow: a hundred
// Kotlin sources leave over 300 MB live for the rest of the JVM. These are shared between parses in the same
// way, so one source still speeds up the next, but are thrown away once they pass maxStates. At 20,000 states
// that holds about 55 MB for a tenth more parse time; clearDFA() is no substitute, it keeps the contexts.
internal class PredictionCaches(private val maxStates: Int = 20_000) {

    private class Caches(atn: ATN) {
        val decisions = Array(atn.numberOfDecisions) { DFA(atn.getDecisionState(it), it) }
        val contexts = PredictionContextCache()
        val states get() = decisions.sumOf { it.states.size }
    }

    @Volatile
    private var current: Caches? = null

    fun <P : Parser> attachTo(parser: P): P = parser.apply {
        val caches = current?.takeIf { it.states <= maxStates } ?: Caches(atn).also { current = it }
        interpreter = ParserATNSimulator(this, atn, caches.decisions, caches.contexts)
    }
}
