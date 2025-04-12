package dev.kensa.example

interface SomeKotlinInterface {
    fun aDefaultFunction() = "DefaultValue"

    fun overrideMe(): String

    fun renderMe(): String
}