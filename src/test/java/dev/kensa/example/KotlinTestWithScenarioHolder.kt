package dev.kensa.example

import dev.kensa.ActionUnderTest
import dev.kensa.Scenario
import dev.kensa.ScenarioHolder
import dev.kensa.kotlin.KotlinKensaTest
import org.junit.jupiter.api.Test

class MyHolder {
    @Scenario
    val myScenario = MyScenario("foopy")
}

class MyScenario(
    val value: String
)

class KotlinTestWithScenarioHolder : KotlinKensaTest {
    fun test(block: MyHolder.() -> Unit): Unit = with(myHolder) { block() }

    @ScenarioHolder
    private val myHolder = MyHolder()

    @Test
    fun simpleTest() {
        test { whenever(actionWith(myScenario.value)) }
    }

    private fun actionWith(value: String) = ActionUnderTest { g, i ->
    }
}