package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor

sealed interface TemplateToken {

    enum class Type(val code: String, val isWhitespace: Boolean = false) {
        Acronym("ac"),
        BlankLine("bl", true),
        BooleanLiteral("bo"),
        CharacterLiteral("cl"),
        Expandable("ex", true),
        FieldValue("fv"),
        Highlighted("hl"),
        Hinted("hi"),
        ProtectedPhrase("pph"),
        Identifier("id"),
        Indent("in", true),
        Keyword("kw"),
        MethodValue("mv"),
        NewLine("nl", true),
        NullLiteral("null"),
        NumberLiteral("num"),
        Operator("op"),
        ParameterValue("pv"),
        FixturesValue("fv"),
        OutputsValueByName("ov"),
        OutputsValueByKey("ov"),
        StringLiteral("sl"),
        TextBlock("tb"),
        Word("wd");
    }

    val template: String
    val emphasis: EmphasisDescriptor
    val types: Set<Type>

    fun hasType(type: Type) = types.contains(type)

    data class SimpleTemplateToken(
        override val template: String,
        override val emphasis: EmphasisDescriptor = EmphasisDescriptor.Default,
        override val types: Set<Type>,
    ) : TemplateToken

    data class RenderedValueToken(
        override val template: String,
        override val emphasis: EmphasisDescriptor = EmphasisDescriptor.Default,
    ) : TemplateToken {
        override val types: Set<Type> = setOf(Type.MethodValue)
    }

    data class NestedTemplateToken(
        override val template: String,
        override val emphasis: EmphasisDescriptor = EmphasisDescriptor.Default,
        override val types: Set<Type>,
        val name: String,
        val nestedTokens: List<List<TemplateToken>>
    ) : TemplateToken {
        var parameterTokens: List<TemplateToken> = emptyList()
    }
}

sealed interface RenderedToken {

    val value: String
    val cssClasses: Set<String>
    val hint: String?

    data class RenderedValueToken(
        override val value: String,
        override val cssClasses: Set<String>,
        override val hint: String? = null
    ) : RenderedToken

    data class RenderedNestedToken(
        override val value: String,
        override val cssClasses: Set<String>,
        override val hint: String? = null,
        val name: String,
        val parameterTokens: List<RenderedToken>,
        val nestedTokens: List<List<RenderedToken>>
    ) : RenderedToken
}

