package dev.kensa

import dev.kensa.context.TestContextHolder.testContext

interface SetupStep {
    fun givens(): List<GivensBuilder> = emptyList()
    fun actions(): List<ActionUnderTest> = emptyList()
    fun verify() {}
}

interface KotestSetupStep : SetupStep, dev.kensa.kotlin.WithKotest
interface HamcrestJavaSetupStep : SetupStep, dev.kensa.java.WithHamcrest
interface HamcrestKotlinSetupStep : SetupStep, dev.kensa.kotlin.WithHamcrest
interface AssertJSetupStep : SetupStep, dev.kensa.java.WithAssertJ

class SetupSteps(vararg steps: SetupStep) {

    private val steps = steps.toList()

    fun execute() {
        with(testContext()) {
            steps.forEach { step ->
                step.givens().forEach { given(it) }
                step.actions().forEach { action ->
                    action.execute(givens, interactions)
                }
                step.verify()
            }
        }
    }
}