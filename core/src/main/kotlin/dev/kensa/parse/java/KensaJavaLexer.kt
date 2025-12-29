package dev.kensa.parse.java

import dev.kensa.parse.KensaLexer
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.Token

class KensaJavaLexer(input: CharStream) : Java20Lexer(input) {

    private val kensaLexer = KensaLexer(
        newlineToken = NL,
        kensaNoteToken = KENSA_NOTE,
        kensaHintTokens = setOf(KENSA_HINT),
        endOfLineTokens = setOf(RPAREN, RBRACE, SEMI),
        identifierToken = Identifier,
        shouldIgnore = { type == WS || type == SEMI }
    )

    override fun nextToken(): Token? = kensaLexer.nextToken { super.nextToken() }
}