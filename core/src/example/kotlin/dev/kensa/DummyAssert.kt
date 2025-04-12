package dev.kensa

class DummyAssert<T : Any> {
    val isNotBlank: Unit
        get() {}

    fun isEqualTo(expected: T?) {}
    fun isIn(vararg expected: T?) {}
    fun contains(expected: String?) {}

    companion object {
        @JvmStatic
        fun <T : Any> assertThat(actual: T?): DummyAssert<T> = DummyAssert()

        fun <T: Any> assertThat(extractor: StateExtractor<T>) : DummyAssert<T> = DummyAssert<T>()
    }
}