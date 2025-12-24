package dev.kensa.parse

import org.antlr.v4.runtime.CommonToken
import org.antlr.v4.runtime.Token

class KensaToken private constructor(token: Token, val hint: String, val note: String) : CommonToken(token) {
    companion object {
        fun withHint(token: Token, hint: String) = KensaToken(token, hint, "")
        fun withNote(token: Token, note: String) = when(token) {
            is KensaToken -> KensaToken(token, token.hint, note)
            else -> KensaToken(token, "", note)
        }
    }
}