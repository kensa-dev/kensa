package dev.kensa.parse

import dev.kensa.parse.KensaToken.Companion.withNote
import org.antlr.v4.runtime.Token
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

class KensaLexer(
    private val newlineToken: Int,
    private val kensaNoteToken: Int,
    private val kensaHintTokens: Set<Int>,
    private val endOfLineTokens: Set<Int>,
    private val identifierToken: Int,
    private val shouldIgnore: Token.() -> Boolean,
) {
    class PreviousToken(val token: Token, val index: Int)

    private val bddPrefixes get() = setOf("given", "when", "whenever", "then", "thenEventually", "thenContinually", "and", "andEventually", "andContinually")

    private var tokenBuffer = ArrayDeque<Token>()
    private val pendingNote: StringBuilder = StringBuilder()
    private val pendingHint: StringBuilder = StringBuilder()

    fun nextToken(nextToken: () -> Token?): Token? =
        with(tokenBuffer) {
            fillAndEvaluate(nextToken)

            takeUnless { isEmpty() }?.run {
                removeFirst()
            }
        }

    private fun ArrayDeque<Token>.fillAndEvaluate(nextToken: () -> Token?) {
        if (isEmpty()) {
            do {
                val token = nextToken()?.also { add(it) }
            } while (token?.type != newlineToken && token?.type != Token.EOF)

            evaluateLineOfTokens()
        }
    }

    private fun ArrayDeque<Token>.evaluateLineOfTokens() {
        var previousToken: PreviousToken? = null
        var wasInterestingLine = false

        for (index in indices) {
            previousToken = previousInterestingToken(index, previousToken)

            with(this[index]) {
                if (shouldIgnore()) continue

                if (tryAppendHintOrNote()) {
                    wasInterestingLine = true
                    if (previousToken == null) {
                        continue
                    }
                }

                if (isHintable() && pendingHint.isNotBlank()) {
                    replaceWithKensaHintToken(this, index)
                }

                if (isNotable() && pendingNote.isNotBlank()) {
                    wasInterestingLine = true
                    replaceWithKensaNoteToken(index)
                }

                if (isKensaNote() && previousToken.canHaveEndOfLineKensaNote()) {
                    replaceWithKensaNoteToken(previousToken.index)
                    break
                }
            }
        }

        if (!wasInterestingLine) {
            pendingNote.clear()
            pendingHint.clear()
        }
    }

    private fun Token.isNotable() = type == identifierToken && isBddKeyword()
    private fun Token.isHintable() = type == identifierToken
    private fun Token.isKensaNote() = type == kensaNoteToken
    private fun Token.isBddKeyword(): Boolean {
        if (type != identifierToken) return false
        val lowercaseText = text.lowercase()
        return bddPrefixes.any { prefix ->
            lowercaseText == prefix || (lowercaseText.startsWith(prefix) && text.length > prefix.length && text[prefix.length].isUpperCase())
        }
    }

    // End-of-line notes are only valid after a closing parenthesis or curly brace
    @OptIn(ExperimentalContracts::class)
    private fun PreviousToken?.canHaveEndOfLineKensaNote(): Boolean {
        contract {
            returns(true) implies (this@canHaveEndOfLineKensaNote != null)
        }
        return this?.token?.type in endOfLineTokens
    }

    private fun Token.tryAppendHintOrNote(): Boolean =
        when (type) {
            kensaNoteToken -> {
                pendingNote.append(this) { asNote() }; true
            }

            in kensaHintTokens -> {
                pendingHint.append(this) { asHint() }; true
            }

            else -> false
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
        if (!previous.shouldIgnore()) return PreviousToken(previous, index - 1)

        return original
    }

    private fun Token.asNote() = text.substring(3).trim()
    private fun Token.asHint() = text.substringAfter("/*+").substringBefore("*/").trim()
    private fun StringBuilder.append(token: Token, fn: Token.() -> String) {
        if (isNotEmpty()) append("\n")
        append(token.fn())
    }
}