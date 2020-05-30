package dev.kensa.sentence

import java.util.*

class Acronym(val acronym: String, val meaning: String) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Acronym) {
            return false
        }
        return acronym == other.acronym
    }

    override fun hashCode(): Int = Objects.hash(acronym)

    companion object {
        @JvmStatic
        fun of(acronym: String, meaning: String): Acronym {
            require((acronym.length > 2)) { "Acronyms must be at least 2 characters. [$acronym]" }
            return Acronym(acronym, meaning)
        }
    }
}