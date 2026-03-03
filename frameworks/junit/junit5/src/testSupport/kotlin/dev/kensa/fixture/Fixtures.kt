package dev.kensa.fixture

import dev.kensa.RenderedValue

class MyScenarioHolder(@field:RenderedValue val scenario: MyScenario)
class MyScenario(val stringValue: String = "aStringValue") {
    override fun toString(): String = "withAScenarioContaining $stringValue"
}

private val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun aString(length: Int = 5): String {
    return (1..length)
        .map { chars.random() }
        .joinToString("")
}

fun aBoolean(): Boolean = listOf(true, false).random()

fun anInt(): Int = (1..1000).random()

fun aLong(): Long = (1..1000).random().toLong()
