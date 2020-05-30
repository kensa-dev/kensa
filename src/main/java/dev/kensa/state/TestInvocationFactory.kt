package dev.kensa.state

import dev.kensa.Kensa
import dev.kensa.context.TestContext
import dev.kensa.parse.ParsedTestInvocation
import dev.kensa.parse.TestInvocationParser
import dev.kensa.parse.java.JavaMethodParser
import dev.kensa.parse.kotlin.KotlinFunctionParser
import dev.kensa.render.diagram.SequenceDiagramFactory
import dev.kensa.render.diagram.svg.SvgCompatiblePredicate.isSvgCompatible
import java.time.Duration

class TestInvocationFactory(
        private val testInvocationParser: TestInvocationParser,
        private val javaMethodParser: JavaMethodParser,
        private val kotlinFunctionParser: KotlinFunctionParser,
        private val sequenceDiagramFactory: SequenceDiagramFactory
) {

    fun create(elapsedTime: Duration, testContext: TestContext, testInvocationContext: TestInvocationContext, throwable: Throwable?): TestInvocation {
        val sequenceDiagram = takeIf { testContext.interactions.containsEntriesMatching(isSvgCompatible()) }
                ?.sequenceDiagramFactory?.create(testContext.interactions)

        return TestInvocation(
                elapsedTime,
                Kensa.configuration.dictionary.acronyms,
                throwable,
                sequenceDiagram,
                parse(testInvocationContext),
                testContext.interactions,
                testContext.givens
        )
    }

    private fun parse(testInvocationContext: TestInvocationContext): ParsedTestInvocation {
        return when {
            testInvocationContext.isKotlin() -> testInvocationParser.parse(testInvocationContext, kotlinFunctionParser)
            else -> testInvocationParser.parse(testInvocationContext, javaMethodParser)
        }
    }
}