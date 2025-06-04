package dev.kensa.example

import dev.kensa.ActionUnderTest
import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.StateExtractor

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

    private fun whenever(action: MyBlock) = Unit

    private fun somethingWith(block: MyBlock.() -> Unit) = MyBlock().apply(block)

    class MyBlock() {
        var aDataItem: String = ""
    }

    private fun fixtures(name: String) = ""

    private fun theExtractedValue() = StateExtractor { "" }

    private fun actionWith(value: String) = ActionUnderTest { _, _, _ ->
    }
}