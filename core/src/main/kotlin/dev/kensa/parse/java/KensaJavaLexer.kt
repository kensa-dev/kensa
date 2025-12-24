package dev.kensa.parse.java

import dev.kensa.parse.Java20Lexer
import dev.kensa.parse.KensaToken
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.Token

class KensaJavaLexer(input: CharStream) : Java20Lexer(input) {

    private var tokenBuffer = ArrayDeque<Token>()
    private val pendingNote: StringBuilder = StringBuilder()
    private val pendingHint: StringBuilder = StringBuilder()

    override fun nextToken(): Token? =
        with(tokenBuffer) {
            fillAndEvaluate()

            takeUnless { isEmpty() }?.run {
                removeFirst()
            }
        }

    private fun ArrayDeque<Token>.fillAndEvaluate() {
        if (isEmpty()) {
            do {
                val token = super.nextToken()
                add(token)
            } while (token.type != Token.EOF)

            evaluateLineOfTokens()
        }
    }

    private fun ArrayDeque<Token>.evaluateLineOfTokens() {
        var previousToken: Token? = null
        for (index in indices) {
            previousToken = previousNonWhitespaceToken(index, previousToken)

            with(this[index]) {
                if (isWhitespaceToken()) continue
                val didAppendHintOrNote = tryAppendHintOrNote()
                if (previousToken == null && didAppendHintOrNote) continue

                if (isHintableToken() && pendingHint.isNotBlank()) {
                    replaceWithKensaHintToken(this, index)
                }

                if (type == Identifier && pendingNote.isNotBlank()) {
                    replaceWithKensaNoteToken(index)
                }

                if (isKensaNote() && previousToken.canHaveEndOfLineKensaNote()) {
                    pendingNote.append(this) { asNote() }
                    replaceWithKensaNoteToken(index - 2)
                    removeAt(index)
                    break
                }
            }
        }
    }

    private fun Token.isKensaNote() = type == KENSA_NOTE

    // End-of-line notes are only valid after a closing parenthesis or curly brace
    private fun Token?.canHaveEndOfLineKensaNote() = this?.type == RPAREN
    private fun Token.isHintableToken() = type == Identifier
    private fun Token.isWhitespaceToken() = type == WS
    private fun Token.tryAppendHintOrNote(): Boolean {
        when (type) {
            KENSA_NOTE -> pendingNote.append(this) { asNote() }
            KENSA_HINT -> pendingHint.append(this) { asHint() }
            else -> return false
        }
        return true
    }

    private fun ArrayDeque<Token>.replaceWithKensaHintToken(token: Token, index: Int) {
        this[index] = KensaToken.withHint(token, pendingHint.toString())
        pendingHint.clear()
    }

    private fun ArrayDeque<Token>.replaceWithKensaNoteToken(index: Int) {
        val original = this[index]
        this[index] = KensaToken.Companion.withNote(original, pendingNote.toString())
        pendingNote.clear()
    }

    private fun ArrayDeque<Token>.previousNonWhitespaceToken(index: Int, original: Token?): Token? {
        if (index == 0) return null

        val previous = this[index - 1]
        if (previous.type != WS) return previous

        return original
    }

    private fun Token.asNote() = text.substring(3).trim()
    private fun Token.asHint() = text.substringAfter("/*+").substringBefore("*/").trim()
    private fun StringBuilder.append(token: Token, fn: Token.() -> String) {
        if (isNotEmpty()) append("\n")
        append(token.fn())
    }
}