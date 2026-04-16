package dev.kensa.state

import dev.kensa.KensaException
import dev.kensa.context.TestContext
import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParseError
import dev.kensa.parse.ParsedInvocation
import dev.kensa.parse.RenderError
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

    fun create(elapsedTime: Duration, testContext: TestContext, testInvocationContext: TestInvocationContext, throwable: Throwable?, displayName: String): Pair<TestInvocation, List<ParseError>> {
        val (parsedInvocation, renderErrors) = try {
            testInvocationParser.parse(testInvocationContext, parser)
        } catch (e: KensaException) {
            parseFailedInvocation(testInvocationContext, e) to emptyList()
        } catch (e: Exception) {
            parseFailedInvocation(testInvocationContext, KensaException("Unexpected error during parsing", e)) to emptyList()
        }
        val parseErrors = try {
            parser.parse(testInvocationContext.method).parseErrors
        } catch (_: Exception) {
            emptyList()
        }
        val invocation = TestInvocation(
            elapsedTime,
            displayName,
            throwable,
            sequenceDiagramFactory.create(testContext.interactions),
            parsedInvocation,
            testContext.interactions,
            testContext.outputs,
            testContext.fixtures,
            renderErrors,
        )
        return invocation to parseErrors
    }

    private fun parseFailedInvocation(testInvocationContext: TestInvocationContext, e: Exception): ParsedInvocation {
        val method = testInvocationContext.method
        val errorDetail = generateSequence(e as Throwable?) { it.cause }.joinToString("\nCaused by: ") { it.message ?: it.javaClass.name }
        val sentences = buildList {
            add(sentence("Kensa was unable to parse this test. This usually means the test contains syntax that the parser does not yet handle."))
            add(sentence("Please copy the details below (together with some example code) and report them at https://github.com/kensa-dev/kensa/issues so we can fix it."))
            add(sentence("--- Error ---"))
            add(sentence(errorDetail))
            add(sentence("--- End ---"))
        }
        return ParsedInvocation(0, method.name, emptyList(), sentences, emptySet(), null)
    }

    private fun sentence(text: String) = RenderedSentence(listOf(RenderedValueToken(text, emptySet())), 0)
}