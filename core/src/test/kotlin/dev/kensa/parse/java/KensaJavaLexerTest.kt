package dev.kensa.parse.java

import dev.kensa.parse.java.Java20Lexer.*
import dev.kensa.parse.KensaToken
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldNotBeInstanceOf
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.Token
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class KensaJavaLexerTest {
    @Nested
    inner class Notes {

        @Test
        fun `single line note comment attaches to next token when next token is a BDD keyword`() {
            val input = """
            /// This is a note for given
            given(() -> {});
            whenever(() -> {});
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].type shouldBe KENSA_NOTE
            tokenList[1].type shouldBe NL
            tokenList[2].shouldBeInstanceOf<KensaToken> {
                it.text shouldBe "given"
                it.note shouldBe "This is a note for given"
            }
            tokenList[3].type shouldBe LPAREN
            tokenList[4].type shouldBe LPAREN
            tokenList[5].type shouldBe RPAREN
            tokenList[6].type shouldBe WS
            tokenList[7].type shouldBe ARROW
            tokenList[8].type shouldBe WS
            tokenList[9].type shouldBe LBRACE
            tokenList[10].type shouldBe RBRACE
            tokenList[11].type shouldBe RPAREN
            tokenList[12].type shouldBe SEMI
            tokenList[13].type shouldBe NL
            tokenList[14].text shouldBe "whenever"
            tokenList[15].type shouldBe LPAREN
            tokenList[16].type shouldBe LPAREN
            tokenList[17].type shouldBe RPAREN
            tokenList[18].type shouldBe WS
            tokenList[19].type shouldBe ARROW
            tokenList[20].type shouldBe WS
            tokenList[21].type shouldBe LBRACE
            tokenList[22].type shouldBe RBRACE
            tokenList[23].type shouldBe RPAREN
            tokenList[24].type shouldBe SEMI
        }

        @Test
        fun `single line note comment does not attach to next token when next token when not a BDD keyword`() {
            val input = """
            /// This is a note for thing but won't be applied
            thing();
            whenever();
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].type shouldBe KENSA_NOTE
            tokenList[1].type shouldBe NL
            tokenList[2].shouldNotBeInstanceOf<KensaToken>()
            tokenList[2].type shouldBe Identifier
            tokenList[3].type shouldBe LPAREN
            tokenList[4].type shouldBe RPAREN
            tokenList[5].type shouldBe SEMI
            tokenList[6].type shouldBe NL
            tokenList[7].text shouldBe "whenever"
            tokenList[8].type shouldBe LPAREN
            tokenList[9].type shouldBe RPAREN
            tokenList[10].type shouldBe SEMI
        }

        @Test
        fun `single line note comment does not attach to next token when separated by blank line`() {
            val input = """
            /// This is a note for given but won't be applied

            given();

            /// This is a note for when
            when();
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].type shouldBe KENSA_NOTE
            tokenList[1].type shouldBe NL
            tokenList[2].type shouldBe NL
            tokenList[3].shouldNotBeInstanceOf<KensaToken>()
            tokenList[3].type shouldBe Identifier
            tokenList[4].type shouldBe LPAREN
            tokenList[5].type shouldBe RPAREN
            tokenList[6].type shouldBe SEMI
            tokenList[7].type shouldBe NL
            tokenList[8].type shouldBe NL
            tokenList[9].type shouldBe KENSA_NOTE
            tokenList[10].type shouldBe NL
            tokenList[11].shouldBeInstanceOf<KensaToken> {
                it.text shouldBe "when"
                it.note shouldBe "This is a note for when"
            }
            tokenList[12].type shouldBe LPAREN
            tokenList[13].type shouldBe RPAREN
            tokenList[14].type shouldBe SEMI
        }

        @Test
        fun `multi line note comments attach to next token and concatenate`() {
            val input = """
            /// This is a note
            /// on
            /// multiple lines
            given();
            whenever();
        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].type shouldBe KENSA_NOTE
            tokenList[1].type shouldBe NL
            tokenList[2].type shouldBe KENSA_NOTE
            tokenList[3].type shouldBe NL
            tokenList[4].type shouldBe KENSA_NOTE
            tokenList[5].type shouldBe NL
            tokenList[6].shouldBeInstanceOf<KensaToken>().should {
                it.text shouldBe "given"
                it.note shouldBe "This is a note\non\nmultiple lines"
            }
            tokenList[7].type shouldBe LPAREN
            tokenList[8].type shouldBe RPAREN
            tokenList[9].type shouldBe SEMI
            tokenList[10].type shouldBe NL
            tokenList[11].text shouldBe "whenever"
            tokenList[12].type shouldBe LPAREN
            tokenList[13].type shouldBe RPAREN
            tokenList[14].type shouldBe SEMI
        }

        @Test
        fun `end of line note attaches to previous token (RPAREN)`() {
            val input = """
            given(); /// 😜
            whenever();

        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].text shouldBe "given"
            tokenList[1].type shouldBe LPAREN
            tokenList[2].shouldBeInstanceOf<KensaToken> {
                it.type shouldBe RPAREN
                it.note shouldBe "😜"
            }
            tokenList[3].type shouldBe SEMI
            tokenList[4].type shouldBe WS
            tokenList[5].type shouldBe KENSA_NOTE
            tokenList[6].type shouldBe NL
            tokenList[7].text shouldBe "whenever"
            tokenList[8].type shouldBe LPAREN
            tokenList[9].type shouldBe RPAREN
            tokenList[10].type shouldBe SEMI
        }

        @Test
        fun `end of line note attaches to previous token (RBRACE)`() {
            val input = """
            given(() -> {
            } /// 😜
            );
            when();

        """.trimIndent()

            val tokenList = tokenListFor(input)

            tokenList[0].text shouldBe "given"
            tokenList[1].type shouldBe LPAREN
            tokenList[2].type shouldBe LPAREN
            tokenList[3].type shouldBe RPAREN
            tokenList[4].type shouldBe WS
            tokenList[5].type shouldBe ARROW
            tokenList[6].type shouldBe WS
            tokenList[7].type shouldBe LBRACE
            tokenList[8].type shouldBe NL
            tokenList[9].shouldBeInstanceOf<KensaToken> {
                it.type shouldBe RBRACE
                it.note shouldBe "😜"
            }
            tokenList[10].type shouldBe WS
            tokenList[11].type shouldBe KENSA_NOTE
            tokenList[12].type shouldBe NL
            tokenList[13].type shouldBe RPAREN
            tokenList[14].type shouldBe SEMI
            tokenList[15].type shouldBe NL
            tokenList[16].text shouldBe "when"
            tokenList[17].type shouldBe LPAREN
            tokenList[18].type shouldBe RPAREN
            tokenList[19].type shouldBe SEMI
            tokenList[20].type shouldBe NL
        }

    }

    private fun tokenListFor(input: String): List<Token> =
        CommonTokenStream(KensaJavaLexer(CharStreams.fromString(input))).run {
            fill()
            tokens.filter { it.type != Token.EOF }
        }

}