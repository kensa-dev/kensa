package dev.kensa.example

open class SomeKotlinSuperClass(private val superField: Int) {
    fun aSuperFunction() = superField
    fun superRenderMe(): Int = superField
}