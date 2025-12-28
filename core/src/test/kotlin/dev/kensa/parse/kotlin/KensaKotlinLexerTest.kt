package dev.kensa.parse.kotlin

import dev.kensa.parse.KensaToken
import dev.kensa.parse.KotlinLexer.*
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.Token.HIDDEN_CHANNEL
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KensaKotlinLexerTest {

    @Nested
    inner class Notes {

        @Test
        fun `single line note comment attaches to next token when next token is a BDD keyword`() {
            val input = """
            /// This is a note for given
            given()
            whenever()
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].type shouldBe KensaNote
            tokenList[1].type shouldBe NL
            tokenList[2].shouldBeInstanceOf<KensaToken> {
                it.text shouldBe "given"
                it.note shouldBe "This is a note for given"
            }
            tokenList[3].type shouldBe LPAREN
            tokenList[4].type shouldBe RPAREN
            tokenList[5].type shouldBe NL
            tokenList[6].text shouldBe "whenever"
            tokenList[7].type shouldBe LPAREN
            tokenList[8].type shouldBe RPAREN
        }
        @Test
        fun `single line note comment does not attach to next token when next token when not a BDD keyword`() {
            val input = """
            /// This is a note for thing but won't be applied
            thing()
            whenever()
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].type shouldBe KensaNote
            tokenList[1].type shouldBe NL
            tokenList[2].shouldNotBeInstanceOf<KensaToken>()
            tokenList[2].type shouldBe Identifier
            tokenList[3].type shouldBe LPAREN
            tokenList[4].type shouldBe RPAREN
            tokenList[5].type shouldBe NL
            tokenList[6].text shouldBe "whenever"
            tokenList[7].type shouldBe LPAREN
            tokenList[8].type shouldBe RPAREN
        }

        @Test
        fun `single line note comment does not attach to next token when separated by blank line`() {
            val input = """
            /// This is a note for given but won't be applied
            
            given()
            
            /// This is a note for when
            whenever()
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].type shouldBe KensaNote
            tokenList[1].type shouldBe NL
            tokenList[2].type shouldBe NL
            tokenList[3].shouldNotBeInstanceOf<KensaToken>()
            tokenList[3].type shouldBe Identifier
            tokenList[4].type shouldBe LPAREN
            tokenList[5].type shouldBe RPAREN
            tokenList[6].type shouldBe NL
            tokenList[7].type shouldBe NL
            tokenList[8].type shouldBe KensaNote
            tokenList[9].type shouldBe NL
            tokenList[10].shouldBeInstanceOf<KensaToken> {
                it.text shouldBe "whenever"
                it.note shouldBe "This is a note for when"
            }
            tokenList[11].type shouldBe LPAREN
            tokenList[12].type shouldBe RPAREN
        }

        @Test
        fun `multi line note comments attach to next token and concatenate`() {
            val input = """
            /// This is a note
            /// on
            /// multiple lines
            given()
            whenever()
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].type shouldBe KensaNote
            tokenList[1].type shouldBe NL
            tokenList[2].type shouldBe KensaNote
            tokenList[3].type shouldBe NL
            tokenList[4].type shouldBe KensaNote
            tokenList[5].type shouldBe NL
            tokenList[6].shouldBeInstanceOf<KensaToken>().should {
                it.text shouldBe "given"
                it.note shouldBe "This is a note\non\nmultiple lines"
            }
            tokenList[7].type shouldBe LPAREN
            tokenList[8].type shouldBe RPAREN
            tokenList[9].type shouldBe NL
            tokenList[10].text shouldBe "whenever"
            tokenList[11].type shouldBe LPAREN
            tokenList[12].type shouldBe RPAREN
        }

        @Test
        fun `end of line note attaches to previous token`() {
            val input = """
            given() /// 😜
            whenever()
            
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].text shouldBe "given"
            tokenList[1].type shouldBe LPAREN
            tokenList[2].shouldBeInstanceOf<KensaToken> {
                it.type shouldBe RPAREN
                it.note shouldBe "😜"
            }
            tokenList[3].type shouldBe WS
            tokenList[4].type shouldBe KensaNote
            tokenList[5].type shouldBe NL
            tokenList[6].text shouldBe "whenever"
            tokenList[7].type shouldBe LPAREN
            tokenList[8].type shouldBe RPAREN
        }

    }

    @Nested
    inner class Hints {

        @Test
        fun `hints attach to following token - start of line`() {
            val input = """
            /*+ format:bold */ given(somethingHappens())
            whenever()
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].channel shouldBe HIDDEN_CHANNEL // Comments are hidden
            tokenList[1].channel shouldBe HIDDEN_CHANNEL // WS are hidden
            tokenList[2].shouldBeInstanceOf<KensaToken> {
                it.text shouldBe "given"
                it.hint shouldBe "format:bold"
            }
            tokenList[3].type shouldBe LPAREN
            tokenList[4].text shouldBe "somethingHappens"
            tokenList[5].type shouldBe LPAREN
            tokenList[6].type shouldBe RPAREN
            tokenList[7].type shouldBe RPAREN
            tokenList[8].type shouldBe NL
            tokenList[9].text shouldBe "whenever"
            tokenList[10].type shouldBe LPAREN
            tokenList[11].type shouldBe RPAREN
        }

        @Test
        fun `hints attach to following token - inside line`() {
            val input = """
            given(/*+ format:bold */ somethingHappens())
            whenever()
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].text shouldBe "given"
            tokenList[1].type shouldBe LPAREN
            tokenList[2].channel shouldBe HIDDEN_CHANNEL // Comments are hidden
            tokenList[3].channel shouldBe HIDDEN_CHANNEL // WS are hidden
            tokenList[4].shouldBeInstanceOf<KensaToken> {
                it.text shouldBe "somethingHappens"
                it.hint shouldBe "format:bold"
            }
            tokenList[5].type shouldBe LPAREN
            tokenList[6].type shouldBe RPAREN
            tokenList[7].type shouldBe RPAREN
            tokenList[8].type shouldBe NL
            tokenList[9].text shouldBe "whenever"
            tokenList[10].type shouldBe LPAREN
            tokenList[11].type shouldBe RPAREN
        }

        @Test
        fun `hints and notes can be mixed`() {
            val input = """
            /// This is a note
            /*+ format:bold */ given(somethingHappens())
            whenever()
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].channel shouldBe HIDDEN_CHANNEL // Comments are hidden
            tokenList[1].type shouldBe NL
            tokenList[2].channel shouldBe HIDDEN_CHANNEL // Comments are hidden
            tokenList[3].channel shouldBe HIDDEN_CHANNEL // WS are hidden
            tokenList[4].shouldBeInstanceOf<KensaToken> {
                it.text shouldBe "given"
                it.note shouldBe "This is a note"
                it.hint shouldBe "format:bold"
            }
            tokenList[5].type shouldBe LPAREN
            tokenList[6].text shouldBe "somethingHappens"
            tokenList[7].type shouldBe LPAREN
            tokenList[8].type shouldBe RPAREN
            tokenList[9].type shouldBe RPAREN
            tokenList[10].type shouldBe NL
            tokenList[11].text shouldBe "whenever"
            tokenList[12].type shouldBe LPAREN
            tokenList[13].type shouldBe RPAREN
        }
    }

    private fun tokenListFor(input: String): List<Token> =
        CommonTokenStream(KensaKotlinLexer(CharStreams.fromString(input))).run {
            fill()
            tokens.filter { it.type != Token.EOF }
        }
}
