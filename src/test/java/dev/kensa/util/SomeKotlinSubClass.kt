package dev.kensa.util

class SomeKotlinSubClass(integer: Int = 0, private val field1: String) : SomeKotlinSuperClass(integer), SomeKotlinInterface {
    private val valueSupplier: () -> String = { field1 }

    fun aFunction() = field1

    override fun overrideMe() = field1

    override fun renderMe(): String = field1
}