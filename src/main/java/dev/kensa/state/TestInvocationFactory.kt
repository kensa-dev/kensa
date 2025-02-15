package dev.kensa.state

import dev.kensa.context.TestContext
import dev.kensa.parse.ParsedInvocation
import dev.kensa.parse.TestInvocationParser
import dev.kensa.parse.java.JavaMethodParser
import dev.kensa.parse.kotlin.KotlinFunctionParser
import dev.kensa.render.diagram.SequenceDiagramFactory
import java.time.Duration

class TestInvocationFactory(
    private val testInvocationParser: TestInvocationParser,
    private val javaMethodParser: JavaMethodParser,
    private val kotlinFunctionParser: KotlinFunctionParser,
    private val sequenceDiagramFactory: SequenceDiagramFactory
) {

    fun create(elapsedTime: Duration, testContext: TestContext, testInvocationContext: TestInvocationContext, throwable: Throwable?, displayName: String): TestInvocation =
        TestInvocation(
            elapsedTime,
            displayName,
            throwable,
            sequenceDiagramFactory.create(testContext.interactions),
            parse(testInvocationContext),
            testContext.interactions,
            testContext.givens
        )

    private fun parse(context: TestInvocationContext): ParsedInvocation =
        when {
            context.isKotlin -> testInvocationParser.parse(context, kotlinFunctionParser)
            else -> testInvocationParser.parse(context, javaMethodParser)
        }
}