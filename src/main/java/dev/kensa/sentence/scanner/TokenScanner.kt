package dev.kensa.sentence.scanner

import dev.kensa.sentence.TokenType.*
import java.util.*

class TokenScanner(private val keywords: Set<String>, private val acronyms: Set<String>) {

    fun scan(string: String): Indices {
        return Indices().apply {
            scanForKeywords(string, this)
            scanForAcronyms(string, this)
            scanForWords(string, this)
        }
    }

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
            if (acronyms.any { it.equals(word, ignoreCase = true) }) {
                indices.put(Acronym, string.indexOf(word), string.indexOf(word) + word.length)
            }
        }
    }

    private fun scanForKeywords(string: String, indices: Indices) {
        val strings = camelCaseSplit(string)
        val word = strings[0]
        if (keywords.any { it.equals(word, ignoreCase = true) }) {
            indices.put(Keyword, string.indexOf(word), string.indexOf(word) + word.length)
        }
    }

    private fun camelCaseSplit(string: String): List<String> {
        return CAMEL_CASE_SPLITTER.split(string)
    }

    companion object {
        private val CAMEL_CASE_SPLITTER = "(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[0-9])|(?<=[0-9])(?=[A-Z])".toRegex()
    }
}