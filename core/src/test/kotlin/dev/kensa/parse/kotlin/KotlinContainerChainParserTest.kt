package dev.kensa.parse.kotlin

import dev.kensa.Configuration
import dev.kensa.context.ExpandableInvocationContext
import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.example.KotlinWithContainerChains
import dev.kensa.parse.CompositeParserDelegate
import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParserCache
import dev.kensa.sentence.TemplateToken.SimpleTemplateToken
import dev.kensa.sentence.TemplateToken.Type.ParameterValue
import dev.kensa.sentence.TemplateToken.Type.FieldValue
import dev.kensa.util.findMethod
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

internal class KotlinContainerChainParserTest {

    private val configuration = Configuration().apply { sourceLocations = listOf(Path("src/example/kotlin")) }

    private fun parse(functionName: String, vararg paramTypes: Class<*>) = MethodParser(
        ParserCache(),
        configuration,
        CompositeParserDelegate(
            configuration.sourceCode,
            listOf(KotlinParserDelegate({ it.simpleIdentifier().text == functionName }, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode))
        )
    ).parse(KotlinWithContainerChains::class.java.findMethod(functionName, *paramTypes))

    private fun parseWithUseCase(functionName: String) = parse(functionName, KotlinWithContainerChains.UseCase::class.java)

    @Test
    fun `renders container chain prefix before call with arguments`() {
        val tokens = parseWithUseCase("chainWithTrailingCall").sentences.single().tokens
        tokens.shouldContain(SimpleTemplateToken("useCase:stub", setOf(ParameterValue)))
        tokens.map { it.template }.shouldContain("sends")
        tokens.map { it.template }.shouldNotContain("stub")
    }

    @Test
    fun `renders container chain without trailing call`() {
        parseWithUseCase("chainWithoutTrailingCall").sentences.single().tokens
            .shouldContain(SimpleTemplateToken("useCase:stub", setOf(ParameterValue)))
    }

    @Test
    fun `renders multi segment parameterless path`() {
        parseWithUseCase("multiSegmentPath").sentences.single().tokens
            .shouldContain(SimpleTemplateToken("useCase:ref.name", setOf(ParameterValue)))
    }

    @Test
    fun `unannotated member renders as words`() {
        val templates = parseWithUseCase("unannotatedMember").sentences.single().tokens.map { it.template }
        templates.shouldNotContain("useCase:plain")
        templates.shouldContain("use")
        templates.shouldContain("case")
        templates.shouldContain("plain")
    }

    @Test
    fun `field container gets prefixed chain`() {
        val tokens = parse("fieldContainerChain").sentences.single().tokens
        tokens.shouldContain(SimpleTemplateToken("heldCase:stub", setOf(FieldValue)))
        tokens.map { it.template }.shouldContain("sends")
    }

    @Test
    fun `callable reference does not render as chain`() {
        val templates = parseWithUseCase("callableReference").sentences.single().tokens.map { it.template }
        templates.shouldNotContain("useCase:stub")
        templates.shouldContain("use")
        templates.shouldContain("case")
    }

    @Test
    fun `safe navigation renders as chain`() {
        parseWithUseCase("safeNavChain").sentences.single().tokens
            .shouldContain(SimpleTemplateToken("useCase:stub", setOf(ParameterValue)))
    }

    @Test
    fun `not-null assertion stops chain consumption`() {
        parseWithUseCase("notNullChain").sentences.single().tokens
            .shouldContain(SimpleTemplateToken("useCase:stub", setOf(ParameterValue)))
    }

    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            ExpandableInvocationContextHolder.bindToCurrentThread(ExpandableInvocationContext())
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            ExpandableInvocationContextHolder.clearFromThread()
        }
    }
}
