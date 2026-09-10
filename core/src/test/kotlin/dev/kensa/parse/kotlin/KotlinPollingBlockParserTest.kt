package dev.kensa.parse.kotlin

import dev.kensa.Configuration
import dev.kensa.context.ExpandableInvocationContext
import dev.kensa.context.ExpandableInvocationContextHolder
import dev.kensa.example.KotlinWithPollingBlocks
import dev.kensa.parse.CompositeParserDelegate
import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParserCache
import dev.kensa.sentence.TemplateSentence
import dev.kensa.sentence.TemplateToken.Type.*
import dev.kensa.sentence.asTemplateToken
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import kotlin.io.path.Path

class KotlinPollingBlockParserTest {

    private fun parse(methodName: String): List<TemplateSentence> {
        val configuration = Configuration().apply { sourceLocations = listOf(Path("src/example/kotlin")) }
        val isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == methodName }
        val parser = MethodParser(
            ParserCache(), configuration, CompositeParserDelegate(
                configuration.sourceCode,
                listOf(KotlinParserDelegate(isTest, configuration.antlrErrorListenerDisabled, configuration.antlrPredicationMode, configuration.sourceCode))
            )
        )
        val parsedMethod = parser.parse(KotlinWithPollingBlocks::class.java.getDeclaredMethod(methodName))
        parsedMethod.parseErrors.shouldBeEmpty()
        return parsedMethod.sentences
    }

    @Test
    fun `eventually block drops the first inner keyword and renders later steps as keywords`() {
        parse("eventuallyBlock").single().tokens shouldBe listOf(
            Keyword.asTemplateToken("Then"), Word.asTemplateToken("eventually"), NumberLiteral.asTemplateToken("2"), Word.asTemplateToken("seconds"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Word.asTemplateToken("the"), Word.asTemplateToken("first"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("first"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Keyword.asTemplateToken("And"), Word.asTemplateToken("the"), Word.asTemplateToken("second"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("second"),
        )
    }

    @Test
    fun `continually block drops the first inner keyword and renders later steps as keywords`() {
        parse("continuallyBlock").single().tokens shouldBe listOf(
            Keyword.asTemplateToken("Then"), Word.asTemplateToken("continually"), NumberLiteral.asTemplateToken("2"), Word.asTemplateToken("seconds"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Word.asTemplateToken("the"), Word.asTemplateToken("first"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("first"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Keyword.asTemplateToken("And"), Word.asTemplateToken("the"), Word.asTemplateToken("second"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("second"),
        )
    }

    @Test
    fun `single collector block renders wrapper then inner step as words`() {
        parse("singleCollectorBlock").single().tokens shouldBe listOf(
            Keyword.asTemplateToken("Then"), Word.asTemplateToken("eventually"), Word.asTemplateToken("the"), Word.asTemplateToken("first"), Word.asTemplateToken("value"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Word.asTemplateToken("and"), Word.asTemplateToken("then"), Word.asTemplateToken("something"),
        )
    }

    @Test
    fun `lazy step inside block drops the first inner keyword and renders later steps as keywords`() {
        parse("lazyStepInsideBlock").single().tokens shouldBe listOf(
            Keyword.asTemplateToken("Then"), Word.asTemplateToken("eventually"), NumberLiteral.asTemplateToken("2"), Word.asTemplateToken("seconds"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Word.asTemplateToken("the"), Word.asTemplateToken("first"), Word.asTemplateToken("spec"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Keyword.asTemplateToken("And"), Word.asTemplateToken("the"), Word.asTemplateToken("second"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("second"),
        )
    }

    @Test
    fun `three step block renders later inner steps as a keyword`() {
        parse("threeStepBlock").single().tokens shouldBe listOf(
            Keyword.asTemplateToken("Then"), Word.asTemplateToken("eventually"), NumberLiteral.asTemplateToken("2"), Word.asTemplateToken("seconds"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Word.asTemplateToken("the"), Word.asTemplateToken("first"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("first"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Keyword.asTemplateToken("And"), Word.asTemplateToken("the"), Word.asTemplateToken("second"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("second"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Keyword.asTemplateToken("And"), Word.asTemplateToken("the"), Word.asTemplateToken("third"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("third"),
        )
    }

    @Test
    fun `block whose first statement is not a step drops the keyword from the first real step`() {
        parse("nonStepFirstStatementBlock").single().tokens shouldBe listOf(
            Keyword.asTemplateToken("Then"), Word.asTemplateToken("eventually"), NumberLiteral.asTemplateToken("2"), Word.asTemplateToken("seconds"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Word.asTemplateToken("order"), Operator.asTemplateToken("="), Word.asTemplateToken("fetch"), Word.asTemplateToken("order"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Word.asTemplateToken("order"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("X"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Keyword.asTemplateToken("And"), Word.asTemplateToken("the"), Word.asTemplateToken("second"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("second"),
        )
    }

    @Test
    fun `eventually block with no duration argument drops the first inner keyword and renders later steps as keywords`() {
        parse("zeroArgEventuallyBlock").single().tokens shouldBe listOf(
            Keyword.asTemplateToken("Then"), Word.asTemplateToken("eventually"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Word.asTemplateToken("the"), Word.asTemplateToken("first"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("first"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Keyword.asTemplateToken("And"), Word.asTemplateToken("the"), Word.asTemplateToken("second"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("second"),
        )
    }

    @Test
    fun `and eventually block drops the first inner keyword and renders later steps as keywords`() {
        parse("andEventuallyBlock").single().tokens shouldBe listOf(
            Keyword.asTemplateToken("And"), Word.asTemplateToken("eventually"), NumberLiteral.asTemplateToken("2"), Word.asTemplateToken("seconds"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Word.asTemplateToken("the"), Word.asTemplateToken("first"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("first"),
            NewLine.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(), Indent.asTemplateToken(),
            Keyword.asTemplateToken("And"), Word.asTemplateToken("the"), Word.asTemplateToken("second"), Word.asTemplateToken("value"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), StringLiteral.asTemplateToken("second"),
        )
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
