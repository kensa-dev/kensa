package dev.kensa.sentence

sealed interface TemplateToken {

    enum class Type(val code: String, val isWhitespace: Boolean = false) {
        Acronym("ac"),
        BooleanLiteral("bo"),
        CharacterLiteral("cl"),
        Note("nt"),
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
        FixturesValue("fx"),
        OutputsValueByName("ov"),
        OutputsValueByKey("ov"),
        StringLiteral("sl"),
        TextBlock("tb"),
        Word("wd"),
        Table("tab");
    }

    val template: String
    val types: Set<Type>

    fun hasType(type: Type) = types.contains(type)

    data class SimpleTemplateToken(
        override val template: String,
        override val types: Set<Type>,
    ) : TemplateToken

    data class RenderedValueToken(
        override val template: String,
    ) : TemplateToken {
        override val types: Set<Type> = setOf(Type.MethodValue)
    }

    data class ExpandableTemplateToken(
        override val template: String,
        override val types: Set<Type>,
        val name: String,
        val expandableTokens: List<List<TemplateToken>>
    ) : TemplateToken {
        var parameterTokens: List<TemplateToken> = emptyList()
    }

    data class TabularTemplateToken(
        override val template: String,
        override val types: Set<Type>,
        val name: String,
        val rows: List<List<TemplateToken>>,
        val headers: List<String> = emptyList()
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

    data class RenderedExpandableToken(
        override val value: String,
        override val cssClasses: Set<String>,
        override val hint: String? = null,
        val name: String,
        val parameterTokens: List<RenderedToken>,
        val expandableTokens: List<List<RenderedToken>>,
    ) : RenderedToken

    data class RenderedExpandableTabularToken(
        override val value: String,
        override val cssClasses: Set<String>,
        override val hint: String? = null,
        val name: String,
        val parameterTokens: List<RenderedToken>,
        val rows: List<List<RenderedToken>>,
        val headers: List<String> = emptyList()
    ) : RenderedToken
}

