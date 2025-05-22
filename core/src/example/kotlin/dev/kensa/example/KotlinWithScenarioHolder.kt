package dev.kensa.example

import dev.kensa.ActionUnderTest
import dev.kensa.Scenario
import dev.kensa.ScenarioHolder

class MyHolder {
    @Scenario
    val myScenario = MyScenario("foopy")
}

class MyScenario(
    val value: String
)

class KotlinWithScenarioHolder {

    fun test(block: MyHolder.() -> Unit): Unit = with(myHolder) { block() }

    @ScenarioHolder
    private val myHolder = MyHolder()

    fun simpleTest() = test {
        actionWith(myScenario.value)
    }

    private fun actionWith(value: String) = ActionUnderTest { g, i ->
    }
}