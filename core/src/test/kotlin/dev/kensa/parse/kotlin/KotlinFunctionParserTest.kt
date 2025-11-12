package dev.kensa.parse.kotlin

import dev.kensa.Configuration
import dev.kensa.context.NestedInvocationContext
import dev.kensa.context.NestedInvocationContextHolder
import dev.kensa.example.*
import dev.kensa.parse.ElementDescriptor.*
import dev.kensa.parse.KotlinParser
import dev.kensa.parse.ParsedNestedMethod
import dev.kensa.sentence.ProtectedPhrase
import dev.kensa.sentence.TemplateSentence
import dev.kensa.sentence.TemplateToken.Type.*
import dev.kensa.sentence.asTemplateToken
import dev.kensa.util.findMethod
import io.kotest.assertions.asClue
import io.kotest.assertions.assertSoftly
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.*

internal class KotlinFunctionParserTest {

    private val configuration = Configuration()

    @BeforeEach
    internal fun setUp() {
        configuration.apply {
            protectedPhrases(
                ProtectedPhrase("House"),
                ProtectedPhrase("City"),
                ProtectedPhrase("Bus"),
                ProtectedPhrase("Dish"),
                ProtectedPhrase("Box"),
                ProtectedPhrase("Toy"),
            )
        }
    }

    @Nested
    inner class ProtectedPhrases {

        @Test
        fun `recognises various protected phrases`() {
            val functionName = "protectedPhrasesTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val method = KotlinWithProtectedPhrases::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentences = listOf(
                TemplateSentence(listOf(Word.asTemplateToken("there"), Word.asTemplateToken("are"), ProtectedPhrase.asTemplateToken("Houses"), Word.asTemplateToken("in"), Word.asTemplateToken("this"), Word.asTemplateToken("street"))),
                TemplateSentence(listOf(Word.asTemplateToken("there"), Word.asTemplateToken("is"), Word.asTemplateToken("a"), ProtectedPhrase.asTemplateToken("House"), Word.asTemplateToken("in"), Word.asTemplateToken("this"), Word.asTemplateToken("street"))),
                TemplateSentence(listOf(Word.asTemplateToken("there"), Word.asTemplateToken("are"), ProtectedPhrase.asTemplateToken("Houses"), Word.asTemplateToken("in"), Word.asTemplateToken("the"), ProtectedPhrase.asTemplateToken("Cities"))),
                TemplateSentence(listOf(Word.asTemplateToken("the"), ProtectedPhrase.asTemplateToken("House"), Word.asTemplateToken("sits"), Word.asTemplateToken("in"), Word.asTemplateToken("the"), ProtectedPhrase.asTemplateToken("City"))),
                TemplateSentence(listOf(Word.asTemplateToken("i"), Word.asTemplateToken("missed"), Word.asTemplateToken("the"), ProtectedPhrase.asTemplateToken("Bus"))),
                TemplateSentence(listOf(Word.asTemplateToken("global"), ProtectedPhrase.asTemplateToken("City"), Word.asTemplateToken("stats"))),
                TemplateSentence(listOf(Word.asTemplateToken("the"), ProtectedPhrase.asTemplateToken("City"), Word.asTemplateToken("of"), Word.asTemplateToken("lights"), Word.asTemplateToken("is"), Word.asTemplateToken("unlike"), Word.asTemplateToken("other"), ProtectedPhrase.asTemplateToken("Cities"))),
                TemplateSentence(listOf(Word.asTemplateToken("a"), Word.asTemplateToken("tasty"), ProtectedPhrase.asTemplateToken("Dish"))),
                TemplateSentence(listOf(Word.asTemplateToken("a"), ProtectedPhrase.asTemplateToken("Box"), Word.asTemplateToken("of"), Word.asTemplateToken("chocolates"))),
                TemplateSentence(
                    listOf(
                        Word.asTemplateToken("wash"),
                        Word.asTemplateToken("the"),
                        ProtectedPhrase.asTemplateToken("Dishes"),
                        Word.asTemplateToken("and"),
                        Word.asTemplateToken("open"),
                        Word.asTemplateToken("the"),
                        ProtectedPhrase.asTemplateToken("Box"),
                        Word.asTemplateToken("of"),
                        ProtectedPhrase.asTemplateToken("Boxes")
                    )
                ),
                TemplateSentence(listOf(Word.asTemplateToken("a"), ProtectedPhrase.asTemplateToken("Toy"), Word.asTemplateToken("car"))),
                TemplateSentence(listOf(Word.asTemplateToken("the"), Word.asTemplateToken("kids"), Word.asTemplateToken("shared"), Word.asTemplateToken("their"), ProtectedPhrase.asTemplateToken("Toys"))),
            )

            parsedMethod.sentences.forEachIndexed { index, sentence ->
                sentence.tokens shouldBe expectedSentences[index].tokens
            }
        }
    }

    @Nested
    inner class Literals {

        @Test
        fun `recognises various literals`() {
            val functionName = "literalTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val method = KotlinWithVariousLiterals::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentences = listOf(
                TemplateSentence(listOf(Word.asTemplateToken("assert"), Word.asTemplateToken("that"), Word.asTemplateToken("the"), Word.asTemplateToken("null"), Word.asTemplateToken("result"), Word.asTemplateToken("is"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), NullLiteral.asTemplateToken("null"))),
                TemplateSentence(listOf(Word.asTemplateToken("assert"), Word.asTemplateToken("that"), Word.asTemplateToken("the"), Word.asTemplateToken("hex"), Word.asTemplateToken("result"), Word.asTemplateToken("is"), Word.asTemplateToken("equal"), Word.asTemplateToken("to"), NumberLiteral.asTemplateToken("0x123"))),
                TemplateSentence(
                    listOf(
                        Word.asTemplateToken("assert"),
                        Word.asTemplateToken("that"),
                        Word.asTemplateToken("the"),
                        Word.asTemplateToken("boolean"),
                        Word.asTemplateToken("result"),
                        Word.asTemplateToken("is"),
                        Word.asTemplateToken("equal"),
                        Word.asTemplateToken("to"),
                        BooleanLiteral.asTemplateToken("true")
                    )
                ),
                TemplateSentence(
                    listOf(
                        Word.asTemplateToken("assert"),
                        Word.asTemplateToken("that"),
                        Word.asTemplateToken("the"),
                        Word.asTemplateToken("character"),
                        Word.asTemplateToken("result"),
                        Word.asTemplateToken("is"),
                        Word.asTemplateToken("equal"),
                        Word.asTemplateToken("to"),
                        CharacterLiteral.asTemplateToken("a")
                    )
                )
            )

            parsedMethod.sentences.forEachIndexed { index, sentence ->
                sentence.tokens shouldBe expectedSentences[index].tokens
            }
        }

    }

    @Nested
    inner class Comments {

        @Test
        fun `recognises kensa-style line comments in various locations`() {
            val functionName = "kensaStyleLineCommentsTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val method = KotlinWithComments::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentences = listOf(
                TemplateSentence(listOf(
                    Comment.asTemplateToken("Line comment at start of block")
                )),
                TemplateSentence(listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    BooleanLiteral.asTemplateToken("true"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    BooleanLiteral.asTemplateToken("true"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Comment.asTemplateToken("Line comment between statements"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Comment.asTemplateToken("Line comment before method call param"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    BooleanLiteral.asTemplateToken("false"),
                    Comment.asTemplateToken("Line comment after method call param"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Line comment after method call"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Line comment after dereference operator"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    BooleanLiteral.asTemplateToken("false"),
                    Comment.asTemplateToken("Line comment at end of statement line"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Word.asTemplateToken("list"),
                    Word.asTemplateToken("of"),
                    NumberLiteral.asTemplateToken("1"),
                    NumberLiteral.asTemplateToken("2"),
                    Word.asTemplateToken("for"),
                    Word.asTemplateToken("each"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),Comment.asTemplateToken("Line comment at start of for loop"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("it"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    Comment.asTemplateToken("Line comment before arg name"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("expected"),
                    Comment.asTemplateToken("Line comment after arg name"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Operator.asTemplateToken("="),
                    Comment.asTemplateToken("Line comment at start of arg value expression"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("it"),
                    Comment.asTemplateToken("Line comment after first operand"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Line comment after operator"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    NumberLiteral.asTemplateToken("0"),
                    Comment.asTemplateToken("Line comment after second operand"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Line comment on own line after final method call param"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Line comment at end of for loop"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Comment.asTemplateToken("Line comment after final statement"),
                )),
            )

            parsedMethod.sentences.forEachIndexed { index, sentence ->
                sentence.tokens shouldBe expectedSentences[index].tokens
            }
        }

        @Test
        fun `ignores standard line comments in various locations`() {
            val functionName = "ignoredStandardLineCommentsTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val method = KotlinWithComments::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentences = listOf(
                TemplateSentence(listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    BooleanLiteral.asTemplateToken("true"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    BooleanLiteral.asTemplateToken("true"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    BooleanLiteral.asTemplateToken("false"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    BooleanLiteral.asTemplateToken("false"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Word.asTemplateToken("list"),
                    Word.asTemplateToken("of"),
                    NumberLiteral.asTemplateToken("1"),
                    NumberLiteral.asTemplateToken("2"),
                    Word.asTemplateToken("for"),
                    Word.asTemplateToken("each"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("it"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("expected"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Operator.asTemplateToken("="),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("it"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    NumberLiteral.asTemplateToken("0"),
                )),
            )

            parsedMethod.sentences.forEachIndexed { index, sentence ->
                sentence.tokens shouldBe expectedSentences[index].tokens
            }
        }

        @Test
        fun `recognises kensa-style delimited comments in various locations`() {
            val functionName = "kensaStyleDelimitedCommentsTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val method = KotlinWithComments::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentences = listOf(
                TemplateSentence(listOf(
                    Comment.asTemplateToken("Delimited comment at start of block")
                )),
                TemplateSentence(listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    BooleanLiteral.asTemplateToken("true"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    BooleanLiteral.asTemplateToken("true"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Comment.asTemplateToken("Delimited comment between statements"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Comment.asTemplateToken("Delimited comment before method call param"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    BooleanLiteral.asTemplateToken("false"),
                    Comment.asTemplateToken("Delimited comment after method call param"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Delimited comment after method call"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Delimited comment after dereference operator"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    BooleanLiteral.asTemplateToken("false"),
                    Comment.asTemplateToken("Delimited comment at end of statement line"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Comment.asTemplateToken("Multi-line\ncomment"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Word.asTemplateToken("list"),
                    Word.asTemplateToken("of"),
                    NumberLiteral.asTemplateToken("1"),
                    NumberLiteral.asTemplateToken("2"),
                    Word.asTemplateToken("for"),
                    Word.asTemplateToken("each"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),Comment.asTemplateToken("Delimited comment at start of for loop"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Delimited comment at start of line"),
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Comment.asTemplateToken("Delimited comment mid-line"),
                    Word.asTemplateToken("it"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    Comment.asTemplateToken("Delimited comment before arg name"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("expected"),
                    Comment.asTemplateToken("Delimited comment after arg name"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Operator.asTemplateToken("="),
                    Comment.asTemplateToken("Delimited comment at start of arg value expression"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("it"),
                    Comment.asTemplateToken("Delimited comment after first operand"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Delimited comment after operator"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    NumberLiteral.asTemplateToken("0"),
                    Comment.asTemplateToken("Delimited comment after second operand"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Delimited comment on own line after final method call param"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Comment.asTemplateToken("Delimited comment at end of for loop"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Comment.asTemplateToken("Delimited comment after final statement"),
                )),
            )

            parsedMethod.sentences.forEachIndexed { index, sentence ->
                sentence.tokens shouldBe expectedSentences[index].tokens
            }
        }

        @Test
        fun `ignores standard delimited comments in various locations`() {
            val functionName = "ignoredStandardDelimitedCommentsTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val method = KotlinWithComments::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentences = listOf(
                TemplateSentence(listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    BooleanLiteral.asTemplateToken("true"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    BooleanLiteral.asTemplateToken("true"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    BooleanLiteral.asTemplateToken("false"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    BooleanLiteral.asTemplateToken("false"),
                )),
                TemplateSentence(listOf(
                    BlankLine.asTemplateToken(),
                    Word.asTemplateToken("list"),
                    Word.asTemplateToken("of"),
                    NumberLiteral.asTemplateToken("1"),
                    NumberLiteral.asTemplateToken("2"),
                    Word.asTemplateToken("for"),
                    Word.asTemplateToken("each"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("it"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("expected"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Operator.asTemplateToken("="),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("it"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    NumberLiteral.asTemplateToken("0"),
                )),
            )

            parsedMethod.sentences.forEachIndexed { index, sentence ->
                sentence.tokens shouldBe expectedSentences[index].tokens
            }
        }
    }

    @Nested
    inner class Scenario {

        @Test
        fun `replaces scenario value in sentence when using scenario with infix function`() {
            val functionName = "testWithInfixScenario"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithScenario::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("the"),
                    Word.asTemplateToken("extracted"),
                    Word.asTemplateToken("value"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    FieldValue.asTemplateToken("myScenario:value")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using scenario field`() {
            val functionName = "testWithScenarioField"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithScenario::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("with"),
                    FieldValue.asTemplateToken("myScenario:value")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using scenario function`() {
            val functionName = "testWithScenarioFunction"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithScenario::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("with"),
                    MethodValue.asTemplateToken("myScenarioFun:value")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using scenario holder`() {
            val functionName = "testWithScenarioHolder"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithScenario::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("with"),
                    FieldValue.asTemplateToken("myScenario:value"),
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }
    }

    @Nested
    inner class Fixtures {

        @Test
        fun `replaces fixture value in sentence when using fixtures inside lambda function`() {
            val functionName = "testWithFixturesInLambda"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithFixtures::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Keyword.asTemplateToken("When"),
                    Word.asTemplateToken("something"),
                    Word.asTemplateToken("with"),
                    NewLine.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Indent.asTemplateToken(),
                    Word.asTemplateToken("a"),
                    Word.asTemplateToken("data"),
                    Word.asTemplateToken("item"),
                    Operator.asTemplateToken("="),
                    FixturesValue.asTemplateToken("MyFixture:")
                )
            )

            with(parsedMethod) {
                sentences.size shouldBe 1
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces fixture value in sentence when using fixtures with infix function`() {
            val functionName = "testWithInfixFixtures"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithFixtures::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("the"),
                    Word.asTemplateToken("extracted"),
                    Word.asTemplateToken("value"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    FixturesValue.asTemplateToken("MyFixture:")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using fixture`() {
            val functionName = "testWithFixtures"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithFixtures::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("with"),
                    FixturesValue.asTemplateToken("MyFixture:")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }

        @Test
        fun `replaces scenario value in sentence when using chained fixture`() {
            val functionName = "testWithChainedFixture"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithFixtures::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("the"),
                    Word.asTemplateToken("extracted"),
                    Word.asTemplateToken("character"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    FixturesValue.asTemplateToken("MyFixture:toString().last()")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }
    }

    @Nested
    inner class Keywords {
        @Test
        internal fun `replaces 'whenever' with 'when' keyword`() {
            val functionName = "simpleTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithWhenever::class.java
            val method = javaClass.findMethod("simpleTest")
            val parsedMethod = parser.parse(method)

            val expectedSentence = TemplateSentence(
                listOf(
                    Keyword.asTemplateToken("When"),
                    Word.asTemplateToken("some"),
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("performed")
                )
            )

            with(parsedMethod) {
                sentences.first().tokens shouldBe expectedSentence.tokens
            }
        }
    }

    @Nested
    inner class Annotations {

        @Test
        internal fun `recognises SentenceValue and Highlight on various Kotlin properties and functions`() {
            val functionName = "simpleTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val javaClass = KotlinWithAnnotations::class.java
            val method = javaClass.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                with(properties) {
                    size shouldBe 5

                    assertSoftly(get("field1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "field1"
                            it.isHighlight.shouldBeTrue()
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("property1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "property1"
                            it.isHighlight.shouldBeTrue()
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("propertyWithGetter")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "propertyWithGetter"
                            it.isHighlight.shouldBeTrue()
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("throwingGetter")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "throwingGetter"
                            it.isHighlight.shouldBeFalse()
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("lazyProperty")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "lazyProperty"
                            it.isHighlight.shouldBeTrue()
                            it.isRenderedValue.shouldBeFalse()
                            it.isParameterizedTestDescription.shouldBeFalse()

                        }
                    }
                }
                with(methods) {
                    methods.size shouldBe 8
                    assertSoftly(get("method1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "method1"
                            it.isHighlight.shouldBeFalse()
                            it.isRenderedValue.shouldBeTrue()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("nested1")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "nested1"
                            it.isHighlight.shouldBeFalse()
                            it.isRenderedValue.shouldBeFalse()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                    assertSoftly(get("internalNested\$core_example")) {
                        shouldNotBeNull()
                        asClue {
                            it.name shouldBe "internalNested\$core_example"
                            it.isHighlight.shouldBeFalse()
                            it.isRenderedValue.shouldBeFalse()
                            it.isParameterizedTestDescription.shouldBeFalse()
                        }
                    }
                }
            }
        }
    }

    @Nested
    inner class Functions {

        @Test
        internal fun `parses expression function`() {
            val functionName = "simpleTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val expectedSentence = TemplateSentence(
                listOf(
                    Keyword.asTemplateToken("Given"),
                    Word.asTemplateToken("some"),
                    Word.asTemplateToken("action"),
                    Word.asTemplateToken("name"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("added"),
                    Word.asTemplateToken("to"),
                    Word.asTemplateToken("givens"),
                )
            )

            val method = KotlinWithExpressionFunction::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(functionName)
                parameters.descriptors.shouldBeEmpty()

                assertSoftly(methods[functionName]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<MethodElementDescriptor>()
                }

                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }

        @Test
        internal fun `parses interface function`() {
            val functionName = "interfaceTestMethod"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    StringLiteral.asTemplateToken("abc"),
                    Word.asTemplateToken("contains"),
                    StringLiteral.asTemplateToken("a")
                )
            )

            val method = KotlinWithInterface::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(functionName)
                parameters.descriptors.shouldBeEmpty()

                assertSoftly(methods[functionName]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<MethodElementDescriptor>()
                }
                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }

        @Test
        internal fun `parses function on class that has test interface`() {
            val functionName = "simpleTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    StringLiteral.asTemplateToken("xyz"),
                    Word.asTemplateToken("contains"),
                    StringLiteral.asTemplateToken("x")
                )
            )

            val method = KotlinWithInterface::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe(functionName)
                parameters.descriptors.shouldBeEmpty()

                with(properties) {
                    assertSoftly(get("field1")) {
                        shouldNotBeNull()
                        shouldBeInstanceOf<PropertyElementDescriptor>()
                    }
                    assertSoftly(get("field2")) {
                        shouldNotBeNull()
                        shouldBeInstanceOf<PropertyElementDescriptor>()
                    }
                    assertSoftly(get("field3")) {
                        shouldNotBeNull()
                        shouldBeInstanceOf<PropertyElementDescriptor>()
                    }
                }

                assertSoftly(methods[functionName]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<MethodElementDescriptor>()
                }

                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }

        @Test
        internal fun `parses internal function`() {
            val functionName = "simpleTest\$core_example"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    StringLiteral.asTemplateToken("true"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("equal"),
                    Word.asTemplateToken("to"),
                    StringLiteral.asTemplateToken("true")
                )
            )

            val method = KotlinWithInternalFunction::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe("simpleTest")
                parameters.descriptors.shouldBeEmpty()
                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }
    }

    @Nested
    inner class Parameters {
        @Test
        internal fun parsesFunctionWithParameters() {
            val functionName = "parameterizedTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val expectedSentence = TemplateSentence(
                listOf(
                    Word.asTemplateToken("assert"),
                    Word.asTemplateToken("that"),
                    Word.asTemplateToken("first"),
                    Word.asTemplateToken("is"),
                    Word.asTemplateToken("in"),
                    StringLiteral.asTemplateToken("a"),
                    StringLiteral.asTemplateToken("b")
                )
            )

            val method = KotlinWithParameters::class.java.findMethod(functionName)
            val parsedMethod = parser.parse(method)

            with(parsedMethod) {
                name.shouldBe("parameterizedTest")

                assertSoftly(parameters.descriptors["first"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
                assertSoftly(parameters.descriptors["second"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
                sentences.first().tokens.shouldBe(expectedSentence.tokens)
            }
        }

        @Test
        internal fun parsesVariousNestedSentenceStyles() {
            // Need to hook into the parser via a standard test function...
            val functionName = "parameterizedTest"
            val parser = KotlinFunctionParser(
                isTest = aFunctionNamed(functionName),
                configuration,
                configuration.antlrErrorListenerDisabled,
                configuration.antlrPredicationMode,
            )

            val testMethod = KotlinWithParameters::class.java.findMethod(functionName)
            val parsedTestMethod = parser.parse(testMethod)

            parsedTestMethod.nestedMethods
                .shouldHaveSize(4)
                .should {
                    verifyExtensionFunction(it, "anExtensionFunction")
                    verifyFunctionWithContextParameter(it, "aFunctionWithContextParametersBeforeFn")
                    verifyFunctionWithContextParameter(it, "aFunctionWithContextParametersBeforeContext")
                    verifyExtensionFunctionWithContextParameters(it, "anExtensionFunctionWithContextParameters")
                }

        }

        private fun verifyExtensionFunction(map: Map<String, ParsedNestedMethod>, functionName: String) {
            map[functionName].shouldNotBeNull {
                assertSoftly(parameters.descriptors["receiver"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
                assertSoftly(parameters.descriptors["first"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
                assertSoftly(parameters.descriptors["second"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
            }
        }

        private fun verifyFunctionWithContextParameter(map: Map<String, ParsedNestedMethod>, functionName: String) {
            map[functionName].shouldNotBeNull {
                assertSoftly(parameters.descriptors["context$1"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
                assertSoftly(parameters.descriptors["first"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
                assertSoftly(parameters.descriptors["second"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
            }
        }

        private fun verifyExtensionFunctionWithContextParameters(map: Map<String, ParsedNestedMethod>, functionName: String) {
            map[functionName].shouldNotBeNull {
                assertSoftly(parameters.descriptors["context$1"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
                assertSoftly(parameters.descriptors["receiver"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
                assertSoftly(parameters.descriptors["first"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
                assertSoftly(parameters.descriptors["second"]) {
                    shouldNotBeNull()
                    shouldBeInstanceOf<ParameterElementDescriptor>()
                }
            }
        }
    }

    private fun aFunctionNamed(functionName: String): (KotlinParser.FunctionDeclarationContext) -> Boolean = { it.simpleIdentifier().text == functionName.substringBefore("$") }

    companion object {
        @BeforeAll
        @JvmStatic
        fun beforeAll() {
            NestedInvocationContextHolder.bindToCurrentThread(NestedInvocationContext())
        }

        @AfterAll
        @JvmStatic
        fun afterAll() {
            NestedInvocationContextHolder.clearFromThread()
        }
    }
}