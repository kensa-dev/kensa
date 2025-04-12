package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor

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

    fun putKeywords(values: Set<String>) {
        values.forEach(this::putKeyword)
    }

    fun indexProtectedPhrases(value: String): List<Triple<Int, Int, EmphasisDescriptor>> {
        val indices = mutableListOf<Triple<Int, Int, EmphasisDescriptor>>()
        protectedPhrases.forEach {
            var result = Regex(Regex.escape(it.value)).find(value)
            while (result != null) {
                indices.add(Triple(result.range.first, result.range.last + 1, it.emphasisDescriptor))
                result = result.next()
            }
        }

        return indices
    }

    fun isAcronym(value: String) = acronyms.any { it.acronym.equals(value, ignoreCase = true) }
    fun findKeywordOrNull(value: String) = keywords.firstOrNull { it.value == value }
    fun isWhen(value: String) = value.equals("when", ignoreCase = true) || value.equals("whenever", ignoreCase = true)
}