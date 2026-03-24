package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.StateCollector

class KotlinWithFixtures {

    private val MyFixture = "MyFixture"

    fun testWithFixtures() {
        actionWith(fixtures(MyFixture))
    }

    fun testWithInfixFixtures() {
        assertThat(theExtractedValue()) isEqualTo fixtures(MyFixture)
    }

    fun testWithFixturesInLambda() {
        whenever(somethingWith {
            aDataItem = fixtures(MyFixture)
        })
    }

    fun testWithChainedFixture() {
        assertThat(theExtractedCharacter()) isEqualTo fixtures(MyFixture).toString().last()
    }

    private fun whenever(action: MyBlock) = Unit

    private fun somethingWith(block: MyBlock.() -> Unit) = MyBlock().apply(block)

    class MyBlock() {
        var aDataItem: String = ""
    }

    private fun fixtures(name: String) = ""

    private fun theExtractedValue() = StateCollector { "" }
    private fun theExtractedCharacter() = StateCollector { 'c' }

    private fun actionWith(value: String) = Action<ActionContext> {}
}