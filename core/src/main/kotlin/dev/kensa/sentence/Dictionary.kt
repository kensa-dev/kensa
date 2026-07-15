package dev.kensa.sentence

import java.util.*

class Dictionary {
    private val protectedPhrases: MutableSet<ProtectedPhrase> = LinkedHashSet()

    fun putProtectedPhrases(vararg phrases: ProtectedPhrase) {
        protectedPhrases.addAll(phrases)
    }

    fun putProtectedPhrases(phrases: Set<ProtectedPhrase>) {
        protectedPhrases.addAll(phrases)
    }

    val protectedPhraseValues: Set<String> get() = protectedPhrases.map { it.value }.toSet()

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
        val boundaries = wordBoundariesOf(text)
        val matchesByStartIndex = TreeMap<Int, ProtectedPhraseMatch>()
        for (protectedPhrase in protectedPhrases) {
            val patterns = buildSearchPatterns(protectedPhrase.value)
            for (pattern in patterns) {
                for (match in pattern.findAll(text)) {
                    val start = match.range.first
                    val endExclusive = match.range.last + 1
                    if (start !in boundaries || endExclusive !in boundaries) continue
                    val existing = matchesByStartIndex[start]
                    if (existing == null || existing.end < endExclusive) {
                        matchesByStartIndex[start] = ProtectedPhraseMatch(start, endExclusive)
                    }
                }
            }
        }
        return matchesByStartIndex.values.toList()
    }

    private fun wordBoundariesOf(text: String): Set<Int> = buildSet {
        add(0)
        add(text.length)
        CAMEL_CASE_SPLITTER.findAll(text).forEach { add(it.range.first) }
        text.forEachIndexed { i, c ->
            if (!c.isLetterOrDigit()) {
                add(i)
                add(i + 1)
            }
        }
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

    companion object {
        internal val CAMEL_CASE_SPLITTER = "(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[0-9])|(?<=[0-9])(?=[A-Z])".toRegex()
    }

    fun isAcronym(value: String) = acronyms.any { it.acronym.equals(value, ignoreCase = true) }
    fun findKeywordOrNull(value: String) = keywords.firstOrNull { it.value == value }
    fun isWhen(value: String) = value.equals("when", ignoreCase = true) || value.equals("whenever", ignoreCase = true)
}