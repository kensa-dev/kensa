package dev.kensa.sentence.scanner

import dev.kensa.sentence.Dictionary
import dev.kensa.sentence.TokenType.*

class TokenScanner(private val dictionary: Dictionary) {

    fun scan(string: String): Pair<String, Indices> =
        normaliseKeywords(string).let {
            Pair(
                it,
                Indices().apply {
                    if (!scanForHighlightedIdentifier(it, this)) {
                        scanForKeywords(it, this)
                        scanForAcronyms(it, this)
                        scanForWords(it, this)
                    }
                }
            )
        }

    private fun scanForHighlightedIdentifier(string: String, indices: Indices) =
        dictionary.findInterestingIdentifierOrNull(string)?.let {
            indices.put(HighlightedIdentifier, 0, string.length, it.emphasisDescriptor)
            true
        } ?: false

    private fun scanForWords(string: String, indices: Indices) {
        val wordList: MutableSet<Index> = HashSet()
        var lastIndexEnd = 0
        for ((_, start, end) in indices) {
            if (lastIndexEnd < start) {
                splitIntoWords(wordList, string.substring(lastIndexEnd, start), lastIndexEnd)
            }
            lastIndexEnd = end
        }
        if (lastIndexEnd < string.length) {
            splitIntoWords(wordList, string.substring(lastIndexEnd), lastIndexEnd)
        }
        indices.putWords(wordList)
    }

    private fun splitIntoWords(words: MutableSet<Index>, segment: String, offset: Int) {
        var segmentOffset = 0
        for (word in camelCaseSplit(segment)) {
            words.add(Index(Word, offset + segmentOffset, offset + segmentOffset + word.length))
            segmentOffset += word.length
        }
    }

    private fun scanForAcronyms(string: String, indices: Indices) {
        for (word in camelCaseSplit(string)) {
            if (dictionary.isAcronym(word)) {
                indices.put(Acronym, string.indexOf(word), string.indexOf(word) + word.length)
            }
        }
    }

    private fun normaliseKeywords(string: String): String {
        val strings = camelCaseSplit(string)
        val word = strings[0]

        return if (dictionary.isWhen(word)) {
            "when" + strings.drop(1).joinToString("")
        } else string
    }

    private fun scanForKeywords(string: String, indices: Indices) {
        val strings = camelCaseSplit(string)
        val word = strings[0]

        dictionary.findKeywordOrNull(word)?.also {
            indices.put(Keyword, string.indexOf(word), string.indexOf(word) + word.length, it.emphasisDescriptor)
        }
    }

    private fun camelCaseSplit(string: String): List<String> = CAMEL_CASE_SPLITTER.split(string)

    companion object {
        private val CAMEL_CASE_SPLITTER = "(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[0-9])|(?<=[0-9])(?=[A-Z])".toRegex()
    }
}