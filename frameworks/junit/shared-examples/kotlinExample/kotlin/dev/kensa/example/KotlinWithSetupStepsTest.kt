package dev.kensa.example

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.kensa.ActionBlockBuilder.Companion.buildActions
import dev.kensa.GivensBlockBuilder.Companion.buildGivens
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.VerificationBlockBuilder.Companion.verify
import dev.kensa.hamkrest.HamkrestSetupStep
import dev.kensa.hamkrest.WithHamkrest
import org.junit.jupiter.api.Test
import dev.kensa.and

class KotlinWithSetupStepsTest : KotlinExampleTest(), WithHamkrest {

    private lateinit var aValue: String
    private lateinit var aValue2: String

    @field:RenderedValue
    private val expectedValue = "aStringValue"

    @field:RenderedValue
    private val expectedValue2 = "aStringValue2"

    @Test
    fun singleSetupStep() {
        given(aTask())

        then(theValue(), equalTo(expectedValue))
    }

    @Test
    fun chainedSetupStep() {
        given(aTask().and(anotherTask()))

        then(theValue(), equalTo(expectedValue))
        then(theValue2(), equalTo(expectedValue2))
    }

    private fun theValue() = StateCollector { aValue }
    private fun theValue2() = StateCollector { aValue2 }

    private fun aTask() = object : HamkrestSetupStep {
        override fun givens() = buildGivens { }

        override fun actions() = buildActions {
            add { aValue = expectedValue }
        }

        override fun verify() = verify {
            assertThat(aValue, equalTo(expectedValue))
        }
    }

    private fun anotherTask() = object : HamkrestSetupStep {
        override fun givens() = buildGivens { }

        override fun actions() = buildActions {
            add { aValue2 = expectedValue2 }
        }

        override fun verify() = verify {
            assertThat(aValue2, equalTo(expectedValue2))
        }
    }
}
