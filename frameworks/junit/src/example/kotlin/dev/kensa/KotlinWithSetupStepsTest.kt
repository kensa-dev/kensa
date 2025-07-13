package dev.kensa

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dev.kensa.ActionsUnderTest.Companion.buildActions
import dev.kensa.GivensBuilders.Companion.buildGivens
import dev.kensa.Verification.Companion.verify
import dev.kensa.hamkrest.HamkrestSetupStep
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.junit.KensaTest
import org.junit.jupiter.api.Test

class KotlinWithSetupStepsTest : KensaTest, WithHamkrest {

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

    private fun theValue() = StateExtractor { interactions -> aValue }
    private fun theValue2() = StateExtractor { interactions -> aValue2 }

    private fun aTask() = object : HamkrestSetupStep {
        override fun givens() = buildGivens {
            add { givens ->
                givens.put("key", expectedValue)
            }
        }

        override fun actions() = buildActions {
            add { givens, interactions ->
                aValue = givens.get<String>("key")!!
            }
        }

        override fun verify() = verify {
            assertThat(aValue, equalTo(expectedValue))
        }
    }

    private fun anotherTask() = object : HamkrestSetupStep {
        override fun givens() = buildGivens {
            add { givens ->
                givens.put("key2", expectedValue2)
            }
        }

        override fun actions() = buildActions {
            add { givens, interactions ->
                aValue2 = givens.get<String>("key2")!!
            }
        }

        override fun verify() = verify {
            assertThat(aValue2, equalTo(expectedValue2))
        }
    }
}

