package dev.kensa

class SomeBuilder {
    fun withSomething(): SomeBuilder = this

    fun build(): Action<GivensContext> = Action { }

    companion object {
        @JvmStatic
        fun someBuilder(): SomeBuilder = SomeBuilder()
    }
}
