package dev.kensa.parse.java

import dev.kensa.parse.Java20Lexer
import dev.kensa.parse.KensaToken
import dev.kensa.parse.KensaToken.Companion.withNote
import org.antlr.v4.runtime.CharStream
import org.antlr.v4.runtime.Token
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class KensaJavaLexer(input: CharStream) : Java20Lexer(input) {

    private val bddPrefixes = setOf("given", "when", "whenever", "then", "thenEventually", "thenContinually", "and", "andEventually", "andContinually")

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
            } while (token.type != NL && token.type != Token.EOF)

            evaluateLineOfTokens()
        }
    }

    class PreviousToken(val token: Token, val index: Int)

    private fun ArrayDeque<Token>.evaluateLineOfTokens() {
        var previousToken: PreviousToken? = null
        var wasInterestingLine = false

        for (index in indices) {
            previousToken = previousInterestingToken(index, previousToken)

            with(this[index]) {
                if (isWhitespaceOrSemiToken()) continue
                if(tryAppendHintOrNote()) {
                    wasInterestingLine = true
                    if(previousToken == null) {
                        continue
                    }
                }

                if (isHintableToken() && pendingHint.isNotBlank()) {
                    replaceWithKensaHintToken(this, index)
                }

                if (isNotableToken() && pendingNote.isNotBlank()) {
                    wasInterestingLine = true
                    replaceWithKensaNoteToken(index)
                }

                if (isKensaNote() && previousToken.canHaveEndOfLineKensaNote()) {
                    replaceWithKensaNoteToken(previousToken.index)
                    break
                }
            }
        }

        if(!wasInterestingLine) {
            pendingNote.clear()
            pendingHint.clear()
        }
    }

    private fun Token.isBddKeyword(): Boolean {
        if (type != Identifier) return false
        val lowercaseText = text.lowercase()
        return bddPrefixes.any { prefix ->
            lowercaseText == prefix || (lowercaseText.startsWith(prefix) && text.length > prefix.length && text[prefix.length].isUpperCase())
        }
    }

    private fun Token.isKensaNote() = type == KENSA_NOTE
    private fun Token.isNotableToken() = type == Identifier && isBddKeyword()

    // End-of-line notes are only valid after a closing parenthesis or curly brace
    @OptIn(ExperimentalContracts::class)
    private fun PreviousToken?.canHaveEndOfLineKensaNote(): Boolean {
        contract {
            returns(true) implies (this@canHaveEndOfLineKensaNote != null)
        }
        return this?.token?.type == RPAREN || this?.token?.type == RBRACE
    }
    private fun Token.isHintableToken() = type == Identifier
    private fun Token.isWhitespaceOrSemiToken() = type == WS || type == NL || type == SEMI
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
        this[index] = withNote(original, pendingNote.toString())
        pendingNote.clear()
    }

    private fun ArrayDeque<Token>.previousInterestingToken(index: Int, original: PreviousToken?): PreviousToken? {
        if (index == 0) return null

        val previous = this[index - 1]
        if (previous.type != WS && previous.type != SEMI) return PreviousToken(previous, index - 1)

        return original
    }

    private fun Token.asNote() = text.substring(3).trim()
    private fun Token.asHint() = text.substringAfter("/*+").substringBefore("*/").trim()
    private fun StringBuilder.append(token: Token, fn: Token.() -> String) {
        if (isNotEmpty()) append("\n")
        append(token.fn())
    }
}