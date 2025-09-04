package dev.kensa.sentence

import java.util.TreeMap
import kotlin.text.RegexOption.IGNORE_CASE

class Dictionary {
    private val protectedPhrases: MutableSet<ProtectedPhrase> = LinkedHashSet()

    fun putProtectedPhrases(vararg phrases: ProtectedPhrase) {
        protectedPhrases.addAll(phrases)
    }

    fun putProtectedPhrases(phrases: Set<ProtectedPhrase>) {
        protectedPhrases.addAll(phrases)
    }

    private val _acronyms: MutableSet<Acronym> = LinkedHashSet()
    val acronyms: Set<Acronym>
        get() = _acronyms

    fun putAcronyms(vararg acronyms: Acronym) {
        _acronyms.addAll(acronyms)
    }

    fun putAcronyms(acronyms: Set<Acronym>) {
        _acronyms.addAll(acronyms)
    }

    fun clearAcronyms() {
        _acronyms.clear()
    }

    private val _keywords: MutableSet<Keyword> = linkedSetOf(
        Keyword("given"),
        Keyword("when"),
        Keyword("then"),
        Keyword("and")
    )
    val keywords: Set<Keyword>
        get() = _keywords

    fun putKeyword(keyWord: Keyword) {
        _keywords.add(keyWord)
    }

    fun putKeyword(value: String) {
        _keywords.add(Keyword(value))
    }

    fun putKeywords(vararg values: String) {
        values.forEach(this::putKeyword)
    }

    fun putKeywords(vararg keywords: Keyword) {
        keywords.forEach(this::putKeyword)
    }

    fun indexProtectedPhrases(value: String): List<ProtectedPhraseMatch> {
        val matchesByStartIndex = TreeMap<Int, ProtectedPhraseMatch>()

        protectedPhrases.forEach { protectedPhrase ->
            val singular = protectedPhrase.value
            val plural = createPluralForm(singular)
            val patterns = listOf(
                Regex(Regex.escape(singular), IGNORE_CASE),
                Regex(Regex.escape(plural), IGNORE_CASE)
            )

            patterns.forEachIndexed { index, pattern ->
                var result = pattern.find(value)
                while (result != null) {
                    val matchedText = result.value
                    val isSingularPattern = index == 0
                    
                    // Only match if this is a singlular pattern match or plural match contains no upper case characters (CamelCase?)
                    if (isSingularPattern || !matchedText.any { it.isUpperCase() }) {
                        val start = result.range.first
                        val end = result.range.last + 1
                        val match = ProtectedPhraseMatch(start, end, protectedPhrase.emphasisDescriptor)
                        // Update if no existing match or if new match is longer (higher end index)
                        val existingMatch = matchesByStartIndex[start]
                        if (existingMatch == null || existingMatch.end < end) {
                            matchesByStartIndex[start] = match
                        }
                    }
                    result = result.next()
                }
            }
        }

        return matchesByStartIndex.values.toList()
    }

    private val yEndingPattern = Regex(".*[bcdfghjklmnpqrstvwxz]y$")
    private val sibilantEndingPattern = Regex(".*(s|x|z|ch|sh)$")

    private fun createPluralForm(word: String): String = when {
        word.matches(yEndingPattern) -> word.substring(0, word.length - 1) + "ies"
        word.matches(sibilantEndingPattern) -> word + "es"
        else -> word + "s"
    }

    fun isAcronym(value: String) = acronyms.any { it.acronym.equals(value, ignoreCase = true) }
    fun findKeywordOrNull(value: String) = keywords.firstOrNull { it.value == value }
    fun isWhen(value: String) = value.equals("when", ignoreCase = true) || value.equals("whenever", ignoreCase = true)
}