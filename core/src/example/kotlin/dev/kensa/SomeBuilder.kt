package dev.kensa

import dev.kensa.state.Givens

class SomeBuilder {
    fun withSomething(): SomeBuilder = this

    fun build(): GivensBuilder = GivensBuilder { givens: Givens, _ -> givens.put("notImportant", "") }

    companion object {
        @JvmStatic
        fun someBuilder(): SomeBuilder = SomeBuilder()
    }
}