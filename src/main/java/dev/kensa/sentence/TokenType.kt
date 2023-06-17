package dev.kensa.sentence

import dev.kensa.util.unCamelToSeparated

enum class TokenType {
    Acronym,
    BlankLine,
    Expandable,
    FieldValue,
    Highlighted,
    HighlightedIdentifier,
    Identifier,
    Indent,
    Keyword,
    Literal,
    MethodValue,
    NewLine,
    Operator,
    ParameterValue,
    ScenarioValue,
    StringLiteral,
    Word;

    fun asCss(): String = "token-${name.unCamelToSeparated()}"
}