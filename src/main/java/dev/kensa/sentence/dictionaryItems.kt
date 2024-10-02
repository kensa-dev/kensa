package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor

sealed interface DictionaryItem {
    val emphasisDescriptor: EmphasisDescriptor
}

data class ProtectedPhrase(val value: String, override val emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) : DictionaryItem

data class Keyword(val value: String, override val emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default) : DictionaryItem {
    init {
        require(value.length > 2 && !value.contains(" ")) { "Invalid keyword [$value]" }
    }
}

data class Acronym private constructor(val acronym: String) {

    lateinit var _meaning: String
        private set
    val meaning: String get() = _meaning

    var _emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default
        private set
    val emphasisDescriptor: EmphasisDescriptor get() = _emphasisDescriptor

    companion object {
        private const val minLength = 2

        @JvmStatic
        @JvmOverloads
        fun of(acronym: String, meaning: String, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default): Acronym = invoke(acronym, meaning, emphasisDescriptor)

        operator fun invoke(acronym: String, meaning: String, emphasisDescriptor: EmphasisDescriptor = EmphasisDescriptor.Default): Acronym {
            require((acronym.length >= minLength)) { "Acronyms must be at least $minLength characters. [$acronym]" }
            return Acronym(acronym).apply {
                this._meaning = meaning
                this._emphasisDescriptor = emphasisDescriptor
            }
        }
    }
}