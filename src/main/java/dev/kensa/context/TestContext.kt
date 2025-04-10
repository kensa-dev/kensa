package dev.kensa.context

import dev.kensa.ActionUnderTest
import dev.kensa.GivensBuilder
import dev.kensa.GivensWithInteractionsBuilder
import dev.kensa.Kensa
import dev.kensa.Tab
import dev.kensa.UseSetupStrategy
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.Givens
import dev.kensa.state.SetupStrategy
import dev.kensa.util.findAnnotation
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class TestContext(val givens: Givens, val interactions: CapturedInteractions) {
    fun given(builder: GivensWithInteractionsBuilder) {
        builder.build(givens, interactions)
    }

    fun given(builder: GivensBuilder) {
        builder.build(givens)
    }

    fun whenever(action: ActionUnderTest) {
        interactions.isUnderTest = true
        action.execute(givens, interactions)
    }

    fun disableInteractionTestGroup() {
        interactions.isUnderTestEnabled = false
    }

    companion object {
        operator fun invoke(testClass: Class<*>, testMethod: Method) = TestContext(Givens(), CapturedInteractions(testMethod.setupStrategy(testClass.setupStrategy(Kensa.configuration.setupStrategy))))

        private fun AnnotatedElement.setupStrategy(default: SetupStrategy): SetupStrategy = findAnnotation<UseSetupStrategy>()?.value ?: default
    }
}