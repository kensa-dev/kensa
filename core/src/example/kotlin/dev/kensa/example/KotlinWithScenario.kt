package dev.kensa.example

import dev.kensa.ActionUnderTest
import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.Scenario
import dev.kensa.ScenarioHolder
import dev.kensa.StateExtractor

class MyHolder {
    @Scenario
    val myScenario = MyScenario("foopy")
}

class MyScenario(
    val value: String
)

class KotlinWithScenario {

    fun test(block: MyHolder.() -> Unit): Unit = with(myHolder) { block() }

    @Scenario
    val myScenario = MyScenario("foopy")

    @ScenarioHolder
    private val myHolder = MyHolder()

    fun testWithScenarioHolder() = test {
        actionWith(myScenario.value)
    }

    fun testWithScenario() {
        actionWith(myScenario.value)
    }

    fun testWithInfixScenario() {
        assertThat(theExtractedValue()) isEqualTo myScenario.value
    }

    private fun theExtractedValue() = StateExtractor { myScenario.value }

    private fun actionWith(value: String) = ActionUnderTest { g, i ->
    }
}