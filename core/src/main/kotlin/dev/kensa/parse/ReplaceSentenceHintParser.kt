package dev.kensa.parse

import dev.kensa.parse.Event.ExitExpression
import dev.kensa.parse.LocatedEvent.Identifier
import dev.kensa.parse.LocatedEvent.PathExpression

class ReplaceSentenceHintParser(private val parseContext: ParseContext) {

    private val gwtKeywords = setOf(
        "given", "when", "whenever", "then", "and",
        "theneventually", "thencontinually", "andeventually", "andcontinually"
    )
    private val interpolationPattern = Regex("""\{([^}]+)}""")

    fun emitEvents(hintText: String, location: Location, stateMachine: ParserStateMachine) {
        var isFirstToken = true

        fun emitWord(word: String) {
            val toEmit = if (isFirstToken && word.lowercase() in gwtKeywords) word.lowercase() else word
            stateMachine.apply(Identifier(location, toEmit))
            isFirstToken = false
        }

        var lastEnd = 0
        for (match in interpolationPattern.findAll(hintText)) {
            hintText.substring(lastEnd, match.range.first).splitToWords().forEach { emitWord(it) }
            val event = parseContext.asEventFromExpression(match.groupValues[1].trim(), location)
            stateMachine.apply(event)
            if (event is PathExpression) stateMachine.apply(ExitExpression)
            isFirstToken = false
            lastEnd = match.range.last + 1
        }
        hintText.substring(lastEnd).splitToWords().forEach { emitWord(it) }
    }

    private fun String.splitToWords() = trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
}
