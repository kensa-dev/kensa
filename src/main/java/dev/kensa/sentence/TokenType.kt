package dev.kensa.sentence

enum class TokenType(private val css: String, val isWhitespace : Boolean = false) {
    Acronym("ac"),
    BlankLine("bl", true),
    Expandable("ex", true),
    FieldValue("fv"),
    Highlighted("hl"),
    HighlightedIdentifier("hlid"),
    Identifier("id"),
    Indent("in", true),
    Keyword("kw"),
    Literal("li"),
    MethodValue("mv"),
    NewLine("nl", true),
    Operator("op"),
    ParameterValue("pv"),
    ScenarioValue("sv"),
    StringLiteral("sl"),
    Word("wd");

    fun asCss(): String = "tk-$css"
}