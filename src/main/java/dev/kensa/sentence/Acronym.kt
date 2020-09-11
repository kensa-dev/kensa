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
        private const val maxLength = 2
        @JvmStatic
        fun of(acronym: String, meaning: String): Acronym {
            require((acronym.length >= maxLength)) { "Acronyms must be at least $maxLength characters. [$acronym]" }
            return Acronym(acronym, meaning)
        }
    }
}