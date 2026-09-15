package dev.kensa.output.unfurl

import dev.kensa.sentence.RenderedSentence
import dev.kensa.state.TestState

private const val MAX_LENGTH = 300

// The card's text: state and context on the first line, then the sentences
// as the report shows them, one per line, top-level tokens only so an
// expandable contributes its label and not what it hides. Hosts clip long
// descriptions themselves, so this cuts once, on a word, and says so.
internal fun unfurlDescription(state: TestState, detail: String, sentences: List<RenderedSentence>): String {
    val lines = listOf("${state.description} · $detail") + sentences.map { sentence ->
        sentence.tokens.map { it.value }.filter { it.isNotBlank() }.joinToString(" ")
    }
    return cutOnWord(lines.joinToString("\n"))
}

private fun cutOnWord(text: String): String {
    if (text.length <= MAX_LENGTH) return text
    val room = MAX_LENGTH - 1
    val cut = text.lastIndexOf(' ', room).takeIf { it > 0 } ?: room
    return text.substring(0, cut) + "…"
}
