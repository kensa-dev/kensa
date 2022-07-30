package dev.kensa.sentence

class Dictionary {
    private val highlightedIdentifiers: MutableSet<HighlightedIdentifier> = LinkedHashSet()

    fun putHighlightedIdentifiers(vararg identifiers: HighlightedIdentifier) {
        highlightedIdentifiers.addAll(identifiers)
    }

    fun putHighlightedIdentifiers(identifiers: Set<HighlightedIdentifier>) {
        highlightedIdentifiers.addAll(identifiers)
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
        Keyword("whenever"),
        Keyword("then"),
        Keyword("and"),
        Keyword("with"),
        Keyword("that")
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

    fun findInterestingIdentifierOrNull(value: String) = highlightedIdentifiers.firstOrNull { it.value == value }
    fun isAcronym(value: String) = acronyms.any { it.acronym.equals(value, ignoreCase = true) }
    fun findKeywordOrNull(value: String) = keywords.firstOrNull { it.value == value }
    fun isWhen(value: String) = value.equals("when", ignoreCase = true) || value.equals("whenever", ignoreCase = true)
}