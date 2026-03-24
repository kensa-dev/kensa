package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.RenderedValue
import dev.kensa.StateCollector

class MyHolder {
    @RenderedValue
    val myScenario = MyScenario("foopy")
}

class MyScenario(
    val value: String
)

class KotlinWithScenario {

    fun test(block: MyHolder.() -> Unit): Unit = with(myHolder) { block() }

    @RenderedValue
    val myScenario = MyScenario("foopy")

    @RenderedValue
    fun myScenarioFun() = MyScenario("foopy")

    @RenderedValue
    private val myHolder = MyHolder()

    fun testWithScenarioHolder() = test {
        actionWith(myScenario.value)
    }

    fun testWithScenarioField() {
        actionWith(myScenario.value)
    }

    fun testWithScenarioFunction() {
        actionWith(myScenarioFun().value)
    }

    fun testWithInfixScenario() {
        assertThat(theExtractedValue()) isEqualTo myScenario.value
    }

    private fun theExtractedValue() = StateCollector { myScenario.value }

    private fun actionWith(value: String) = Action<ActionContext> {}
}