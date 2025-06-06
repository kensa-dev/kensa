package dev.kensa.example

import dev.kensa.ActionUnderTest
import dev.kensa.DummyAssert.Companion.assertThat
import dev.kensa.RenderedValue
import dev.kensa.StateExtractor

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

    private fun actionWith(value: String) = ActionUnderTest { _, _ ->
    }
}