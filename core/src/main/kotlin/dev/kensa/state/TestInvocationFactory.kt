package dev.kensa.state

import dev.kensa.context.TestContext
import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParsedInvocation
import dev.kensa.parse.TestInvocationParser
import dev.kensa.render.diagram.SequenceDiagramFactory
import dev.kensa.sentence.RenderedSentence
import dev.kensa.sentence.RenderedToken.RenderedValueToken
import kotlin.time.Duration

class TestInvocationFactory(
    private val testInvocationParser: TestInvocationParser,
    private val parser: MethodParser,
    private val sequenceDiagramFactory: SequenceDiagramFactory
) {

    fun create(elapsedTime: Duration, testContext: TestContext, testInvocationContext: TestInvocationContext, throwable: Throwable?, displayName: String): TestInvocation {
        var parseException: Exception? = null
        val parsedInvocation = try {
            testInvocationParser.parse(testInvocationContext, parser)
        } catch (e: Exception) {
            parseException = e
            parseFailedInvocation(testInvocationContext, e)
        }
        return TestInvocation(
            elapsedTime,
            displayName,
            throwable ?: parseException,
            sequenceDiagramFactory.create(testContext.interactions),
            parsedInvocation,
            testContext.interactions,
            testContext.outputs,
            testContext.fixtures,
            parseException,
        )
    }

    private fun parseFailedInvocation(testInvocationContext: TestInvocationContext, e: Exception): ParsedInvocation {
        val method = testInvocationContext.method
        val errorDetail = generateSequence(e as Throwable?) { it.cause }.joinToString("\nCaused by: ") { it.message ?: it.javaClass.name }
        val sentences = buildList {
            add(sentence("Kensa was unable to parse this test. This usually means the test contains syntax that the parser does not yet handle."))
            add(sentence("To suppress this build failure while the issue is fixed, annotate the test method or class with @SuppressParseErrors."))
            add(sentence("Please copy the details below (together with some example code) and report them at https://github.com/kensa-dev/kensa/issues so we can fix it."))
            add(sentence("--- Error ---"))
            add(sentence(errorDetail))
            add(sentence("--- End ---"))
        }
        return ParsedInvocation(0, method.name, emptyList(), sentences, emptySet(), null)
    }

    private fun sentence(text: String) = RenderedSentence(listOf(RenderedValueToken(text, emptySet())), 0)
}