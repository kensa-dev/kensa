package dev.kensa.sentence

sealed interface DictionaryItem

data class ProtectedPhrase(val value: String) : DictionaryItem

data class Keyword(val value: String) : DictionaryItem {
    init {
        require(value.length > 2 && !value.contains(" ")) { "Invalid keyword [$value]" }
    }
}

data class Acronym(val acronym: String) : DictionaryItem {

    lateinit var _meaning: String
        private set
    val meaning: String get() = _meaning

    companion object {
        private const val minLength = 2

        @JvmStatic
        fun of(acronym: String, meaning: String): Acronym = invoke(acronym, meaning)

        operator fun invoke(acronym: String, meaning: String): Acronym {
            require((acronym.length >= minLength)) { "Acronyms must be at least $minLength characters. [$acronym]" }
            return Acronym(acronym).apply {
                this._meaning = meaning
            }
        }
    }
}
