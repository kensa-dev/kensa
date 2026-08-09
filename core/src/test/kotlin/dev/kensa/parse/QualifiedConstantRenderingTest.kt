package dev.kensa.parse

import dev.kensa.Configuration
import dev.kensa.context.ExpandableInvocationContext
import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.example.JavaWithQualifiedConstants
import dev.kensa.example.KotlinWithQualifiedConstants
import dev.kensa.parse.java.Java20Parser
import dev.kensa.parse.java.JavaParserDelegate
import dev.kensa.parse.kotlin.KotlinParser
import dev.kensa.parse.kotlin.KotlinParserDelegate
import dev.kensa.sentence.TemplateToken.ExpandableTemplateToken
import dev.kensa.sentence.TemplateToken.HintedTemplateToken
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class QualifiedConstantRenderingTest {

    private fun parse(methodName: String): ParsedMethod {
        val configuration = Configuration().apply { sourceLocations = listOf(Path("src/example/kotlin")) }
        val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == methodName }
        val parser = MethodParser(
            ParserCache(),
            configuration,
            CompositeParserDelegate(
                configuration.sourceCode,
                listOf(KotlinParserDelegate(isTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode))
            )
        )
        return parser.parse(KotlinWithQualifiedConstants::class.java.getDeclaredMethod(methodName))
    }

    private fun parseJava(methodName: String): ParsedMethod {
        val configuration = Configuration().apply { sourceLocations = listOf(Path("src/example/java")) }
        val isClassTest: (Java20Parser.MethodDeclarationContext) -> Boolean =
            { it.methodHeader()?.methodDeclarator()?.identifier()?.text == methodName }
        val parser = MethodParser(
            ParserCache(),
            configuration,
            CompositeParserDelegate(
                configuration.sourceCode,
                listOf(
                    JavaParserDelegate(
                        isClassTest = isClassTest,
                        isInterfaceTest = { false },
                        configuration.antlrErrorListenerDisabled,
                        configuration.antlrPredicationMode,
                        configuration.sourceCode
                    )
                )
            )
        )
        return parser.parse(JavaWithQualifiedConstants::class.java.getDeclaredMethod(methodName))
    }

    @Test
    fun `qualified enum constants render as hinted member tokens`() {
        val sentences = parse("testWithQualifiedEnums").sentences

        val hinted = sentences.flatMap { it.tokens }.filterIsInstance<HintedTemplateToken>()
        hinted shouldHaveSize 2
        hinted[0] shouldBe HintedTemplateToken("PENDING", "OrderStatus")
        hinted[1] shouldBe HintedTemplateToken("PENDING", "PaymentStatus")
        sentences.flatMap { it.tokens }.none { it.template == "status" } shouldBe true
        sentences.flatMap { it.tokens }.count { it.template == "PENDING" } shouldBe hinted.size
    }

    @Test
    fun `sealed data object renders as hinted member token`() {
        val sentences = parse("testWithSealedDataObject").sentences

        sentences.flatMap { it.tokens }.filterIsInstance<HintedTemplateToken>()
            .single() shouldBe HintedTemplateToken("Pending", "DeliveryStatus")
    }

    @Test
    fun `companion const renders unchanged`() {
        val sentences = parse("testWithCompanionConst").sentences

        val tokens = sentences.flatMap { it.tokens }
        tokens.filterIsInstance<HintedTemplateToken>() shouldHaveSize 0
        tokens.any { it.template == "limits" } shouldBe true
    }

    @Test
    fun `static method call on enum renders unchanged`() {
        val sentences = parse("testWithEnumMethodCall").sentences

        sentences.flatMap { it.tokens }.filterIsInstance<HintedTemplateToken>() shouldHaveSize 0
    }

    @Test
    fun `expandable sentence argument renders as hinted member token`() {
        val sentences = parse("testWithExpandableArgument").sentences

        val expandable = sentences.flatMap { it.tokens }.filterIsInstance<ExpandableTemplateToken>().single()
        expandable.parameterTokens.filterIsInstance<HintedTemplateToken>()
            .single() shouldBe HintedTemplateToken("PENDING", "OrderStatus")
    }

    @Test
    fun `kotlin chained access on enum renders unchanged`() {
        val sentences = parse("testWithChainedAccess").sentences

        val tokens = sentences.flatMap { it.tokens }
        tokens.filterIsInstance<HintedTemplateToken>() shouldHaveSize 0
        tokens.any { it.template == "PENDING" } shouldBe true
        tokens.any { it.template == "name" } shouldBe true
    }

    @Test
    fun `java qualified enum constants render as hinted member tokens`() {
        val sentences = parseJava("testWithQualifiedEnums").sentences

        val hinted = sentences.flatMap { it.tokens }.filterIsInstance<HintedTemplateToken>()
        hinted shouldHaveSize 2
        hinted[0] shouldBe HintedTemplateToken("PENDING", "JavaOrderStatus")
        hinted[1] shouldBe HintedTemplateToken("PENDING", "JavaPaymentStatus")
        sentences.flatMap { it.tokens }.count { it.template == "PENDING" } shouldBe hinted.size
    }

    @Test
    fun `java static method call on enum renders unchanged`() {
        val sentences = parseJava("testWithEnumMethodCall").sentences

        sentences.flatMap { it.tokens }.filterIsInstance<HintedTemplateToken>() shouldHaveSize 0
    }

    @Test
    fun `java chained access on enum renders unchanged`() {
        val sentences = parseJava("testWithChainedAccess").sentences

        sentences.flatMap { it.tokens }.filterIsInstance<HintedTemplateToken>() shouldHaveSize 0
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
