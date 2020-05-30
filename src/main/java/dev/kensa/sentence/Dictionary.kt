package dev.kensa.sentence

import java.util.*

class Dictionary {
    private val _acronyms: MutableSet<Acronym> = LinkedHashSet()
    val acronyms: Set<Acronym>
        get() = _acronyms
    val acronymStrings: Set<String>
        get() = _acronyms.map(Acronym::acronym).toSet()

    fun putAcronyms(vararg acronyms: Acronym) {
        this._acronyms.addAll(acronyms)
    }

    fun putAcronyms(acronyms: Set<Acronym>) {
        this._acronyms.addAll(acronyms)
    }

    fun clearAcronyms() {
        _acronyms.clear()
    }

    private val _keywords: MutableSet<String> = linkedSetOf("given", "when", "whenever", "then", "and", "with", "that")
    val keywords: Set<String>
        get() = _keywords

    fun putKeyword(value: String) {
        require(value.length > 2 && !value.contains(" ")) { "Invalid keyword [$value]" }
        _keywords.add(value)
    }

    fun putKeywords(vararg values: String) {
        values.forEach(this::putKeyword)
    }

    fun putKeywords(values: Set<String>) {
        values.forEach(this::putKeyword)
    }
}