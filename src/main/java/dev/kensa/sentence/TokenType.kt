package dev.kensa.sentence

enum class TokenType(private val css: String) {
    Acronym("ac"),
    BlankLine("bl"),
    Expandable("ex"),
    FieldValue("fv"),
    Highlighted("hl"),
    HighlightedIdentifier("hlid"),
    Identifier("id"),
    Indent("in"),
    Keyword("kw"),
    Literal("li"),
    MethodValue("mv"),
    NewLine("nl"),
    Operator("op"),
    ParameterValue("pv"),
    ScenarioValue("sv"),
    StringLiteral("sl"),
    Word("wd");

    fun asCss(): String = "tk-$css"
}