package dev.kensa.parse.kotlin

import dev.kensa.parse.KensaLexer
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.Token

class KensaKotlinLexer(input: CharStream) : KotlinLexer(input) {

    private val kensaLexer = KensaLexer(
        newlineToken = NL,
        kensaNoteToken = KensaNote,
        kensaHintTokens = setOf(KensaHint, Inside_KensaHint),
        endOfLineTokens = setOf(RPAREN, RCURL),
        identifierToken = Identifier,
        shouldIgnore = { type == WS }
    )

    override fun nextToken(): Token? = kensaLexer.nextToken { super.nextToken() }
}