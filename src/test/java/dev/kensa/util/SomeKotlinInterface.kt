package dev.kensa.util

interface SomeKotlinInterface {
    fun aDefaultFunction() = "DefaultValue"

    fun overrideMe(): String

    fun renderMe(): String
}