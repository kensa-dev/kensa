package dev.kensa.example

import dev.kensa.ExpandableRenderedValue
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

    fun chainedAfterArgumentlessExpandable() {
        assertThatHas(withTheExpectedFields().and(aFirstName of "John").and(aLastName of "Smith"))
    }

    fun chainedAfterExpandableWithArguments() {
        assertThatHas(withTheExpectedFields("Mr").and(aFirstName of "John"))
    }

    fun chainedAfterArgumentlessExpandableValue() {
        assertThatHas(theObservedDetails().and(aFirstName of "John"))
    }

    fun chainedAfterArgumentlessNavigatedExpandable() {
        assertThatHas(this.withTheExpectedFields().and(aFirstName of "John"))
    }

    fun chainedAfterNavigatedExpandableWithArguments() {
        assertThatHas(this.withTheExpectedFields("Mr").and(aFirstName of "John"))
    }

    @ExpandableSentence
    fun theCorrectDetails() = arrayOf(aFirstName of "John", aLastName of "Smith")

    @ExpandableSentence
    fun theCorrectDetailsViaMatchers() = myMatchers(aFirstName, aLastName)

    @ExpandableSentence
    fun withTheExpectedFields(@RenderedValue aTitle: String = "Mr") = Matchers()

    @ExpandableRenderedValue
    fun theObservedDetails(aTitle: String = "Mr") = Matchers()

    @ExpandableSentence
    fun aCombinationOfCalls() = anA() + aB()

    private infix fun String.of(value: String): Matcher = Matcher()
    private fun myMatchers(vararg values: String): Array<Matcher> = arrayOf()
    private fun assertThatHas(matchers: Array<Matcher>) = Unit
    private fun assertThatHas(matchers: Matchers) = Unit
    private fun anA(): Matchers = Matchers()
    private fun aB(): Matchers = Matchers()

    class Matcher
    class Matchers {
        operator fun plus(other: Matchers): Matchers = this
        fun and(other: Matcher): Matchers = this
    }
}
