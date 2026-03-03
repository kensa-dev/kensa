package dev.kensa.example

import com.natpryce.hamkrest.equalTo
import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.GivensContext
import dev.kensa.RenderedValue
import dev.kensa.StateCollector
import dev.kensa.example.KotlinCapturedOutputs.StringOutput
import dev.kensa.example.MoreKotlinCapturedOutputs.BooleanOutput
import dev.kensa.hamkrest.WithHamkrest
import dev.kensa.outputs.CapturedOutputContainer
import dev.kensa.outputs.CapturedOutputsRegistry.registerCapturedOutputs
import dev.kensa.outputs.capturedOutput
import org.junit.jupiter.api.Test

class KotlinWithCapturedOutputsTest : KotlinExampleTest(), WithHamkrest {

    init {
        registerCapturedOutputs(KotlinCapturedOutputs, MoreKotlinCapturedOutputs)
    }

    private val theStringOutput = "theOutput"
    private val theBooleanOutput = true

    @Test
    fun test1() {
        given(somePrerequisites())

        whenever(theOutputsAreGenerated())

        then(theStringOutput(), equalTo(outputs(StringOutput)))
        then(theBooleanFixture(), equalTo(outputs(MoreKotlinCapturedOutputs.BooleanOutput)))
    }

    @Test
    fun test2() {
        given(somePrerequisites())

        whenever(theOutputsAreGenerated())

        then(theStringOutput(), equalTo(outputs("KotlinStringOutput")))
        then(theBooleanFixture(), equalTo(outputs("KotlinBooleanOutput")))
    }

    @Test
    fun test3() {
        given(somePrerequisites())

        whenever(theOutputsAreGenerated())

        then(theConcatenatedOutputs(), equalTo(concatenate(outputs(StringOutput), outputs(BooleanOutput))))
    }

    private fun somePrerequisites(): Action<GivensContext> = Action { context: GivensContext? -> }

    private fun theOutputsAreGenerated(): Action<ActionContext> {
        return Action { (_, _, outputs) ->
            outputs.put(StringOutput, theStringOutput)
            outputs.put(BooleanOutput, theBooleanOutput)
        }
    }

    @RenderedValue
    private fun concatenate(string: String, boolean: Boolean) = "$string-$boolean"
    private fun theConcatenatedOutputs() = StateCollector { concatenate(outputs(StringOutput), outputs(BooleanOutput)) }

    private fun theStringOutput() = StateCollector { it.outputs[StringOutput] }

    private fun theBooleanFixture() = StateCollector { it.outputs[BooleanOutput] }
}

internal object KotlinCapturedOutputs : CapturedOutputContainer {
    val StringOutput = capturedOutput<String>("KotlinStringOutput")
}

internal object MoreKotlinCapturedOutputs : CapturedOutputContainer {
    val BooleanOutput = capturedOutput<Boolean>("KotlinBooleanOutput")
}