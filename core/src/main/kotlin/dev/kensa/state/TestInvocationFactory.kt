package dev.kensa.state

import dev.kensa.context.TestContext
import dev.kensa.parse.MethodParser
import dev.kensa.parse.TestInvocationParser
import dev.kensa.render.diagram.SequenceDiagramFactory
import kotlin.time.Duration

class TestInvocationFactory(
    private val testInvocationParser: TestInvocationParser,
    private val parser: MethodParser,
    private val sequenceDiagramFactory: SequenceDiagramFactory
) {

    fun create(elapsedTime: Duration, testContext: TestContext, testInvocationContext: TestInvocationContext, throwable: Throwable?, displayName: String) =
        TestInvocation(
            elapsedTime,
            displayName,
            throwable,
            sequenceDiagramFactory.create(testContext.interactions),
            testInvocationParser.parse(testInvocationContext, parser),
            testContext.interactions,
            testContext.givens,
            testContext.outputs,
            testContext.fixtures
        )
}