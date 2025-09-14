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

    fun indexProtectedPhrases(text: String): List<ProtectedPhraseMatch> {
        val matchesByStartIndex = TreeMap<Int, ProtectedPhraseMatch>()
        for (protectedPhrase in protectedPhrases) {
            val patterns = buildSearchPatterns(protectedPhrase.value)
            for (pattern in patterns) {
                for (match in pattern.findAll(text)) {
                    val start = match.range.first
                    val endExclusive = match.range.last + 1
                    val existing = matchesByStartIndex[start]
                    if (existing == null || existing.end < endExclusive) {
                        matchesByStartIndex[start] = ProtectedPhraseMatch(start, endExclusive, protectedPhrase.emphasisDescriptor)
                    }
                }
            }
        }
        return matchesByStartIndex.values.toList()
    }

    private fun buildSearchPatterns(singular: String): List<Regex> {
        val (pluralStem, pluralSuffix) = pluralPartsOf(singular)
        val base = Regex.escape(singular)
        val stem = Regex.escape(pluralStem)
        val suffix = Regex.escape(pluralSuffix)
        return listOf(
            Regex("(?i)$base"),
            Regex("(?i)$stem(?-i)$suffix")
        )
    }

    private fun pluralPartsOf(singular: String): Pair<String, String> = when {
        singular.matches(yEndingPattern) -> singular.dropLast(1) to "ies"
        singular.matches(sibilantEndingPattern) -> singular to "es"
        else -> singular to "s"
    }

    private val yEndingPattern = Regex(".*[bcdfghjklmnpqrstvwxz]y$")
    private val sibilantEndingPattern = Regex(".*(s|x|z|ch|sh)$")

    fun isAcronym(value: String) = acronyms.any { it.acronym.equals(value, ignoreCase = true) }
    fun findKeywordOrNull(value: String) = keywords.firstOrNull { it.value == value }
    fun isWhen(value: String) = value.equals("when", ignoreCase = true) || value.equals("whenever", ignoreCase = true)
}