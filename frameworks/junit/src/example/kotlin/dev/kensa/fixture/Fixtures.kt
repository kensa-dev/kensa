package dev.kensa.fixture

import dev.kensa.Resolve

class MyScenarioHolder(@field:Resolve val scenario: MyScenario)
class MyScenario(val stringValue: String = "aStringValue")

private val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun aString(length: Int = 5): String {
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

fun aBoolean(): Boolean = listOf(true, false).random()

fun anInt(): Int = (1..1000).random()

fun aLong(): Long = (1..1000).random().toLong()
