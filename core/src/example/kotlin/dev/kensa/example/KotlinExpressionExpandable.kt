package dev.kensa.example

import dev.kensa.ExpandableSentence
import dev.kensa.RenderedValue

class KotlinExpressionExpandable {

    @RenderedValue
    private val aFirstName = "John"

    @RenderedValue
    private val aLastName = "Smith"

    fun simpleTest() {
        assertThatHas(theCorrectDetails())
    }

    fun expressionBodiedTest() = assertThatHas(theCorrectDetails())

    @ExpandableSentence
    fun theCorrectDetails() = arrayOf(aFirstName of "John", aLastName of "Smith")

    @ExpandableSentence
    fun theCorrectDetailsViaMatchers() = myMatchers(aFirstName, aLastName)

    @ExpandableSentence
    fun aCombinationOfCalls() = anA() + aB()

    private infix fun String.of(value: String): Matcher = Matcher()
    private fun myMatchers(vararg values: String): Array<Matcher> = arrayOf()
    private fun assertThatHas(matchers: Array<Matcher>) = Unit
    private fun anA(): Matchers = Matchers()
    private fun aB(): Matchers = Matchers()

    class Matcher
    class Matchers {
        operator fun plus(other: Matchers): Matchers = this
    }
}
