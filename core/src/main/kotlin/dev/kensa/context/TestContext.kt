package dev.kensa.context

import dev.kensa.*
import dev.kensa.attachments.Attachments
import dev.kensa.fixture.Fixtures
import dev.kensa.outputs.CapturedOutputs
import dev.kensa.state.CapturedInteractions
import dev.kensa.state.SetupStrategy
import dev.kensa.util.findAnnotation
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Method

class TestContext(
    val interactions: CapturedInteractions,
    override val fixtures: Fixtures,
    override val outputs: CapturedOutputs,
    val attachments: Attachments = Attachments()
) : WithFixturesAndOutputs {

    override val fixturesAndOutputs = FixturesAndOutputs(fixtures, outputs)
    val givensContext = GivensContext(fixtures, outputs)
    val actionContext = ActionContext(fixtures, interactions, outputs)
    val collectorContext = CollectorContext(fixtures, interactions, outputs)

    fun given(action: Action<GivensContext>) {
        action.execute(givensContext)
    }

    fun given(steps: SetupSteps) {
        steps.forEach { step ->
            step.givens().buildWith(givensContext).executeWith(givensContext)
            step.actions().buildWith(actionContext).executeWith(actionContext)
            step.verify().verifyWith(collectorContext)
        }
    }

    fun whenever(action: Action<ActionContext>) {
        interactions.isUnderTest = true
        action.execute(actionContext)
    }

    fun disableInteractionTestGroup() {
        interactions.isUnderTestEnabled = false
    }

    companion object {
        operator fun invoke(testClass: Class<*>, testMethod: Method, setupStrategy: SetupStrategy) =
            TestContext(CapturedInteractions(testMethod.setupStrategy(testClass.setupStrategy(setupStrategy))), Fixtures(), CapturedOutputs())

        private fun AnnotatedElement.setupStrategy(default: SetupStrategy): SetupStrategy = findAnnotation<UseSetupStrategy>()?.value ?: default
    }
}