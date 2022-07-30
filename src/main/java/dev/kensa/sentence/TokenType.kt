package dev.kensa.sentence

import dev.kensa.util.unCamelToSeparated

enum class TokenType {
    Acronym,
    Expandable,
    FieldValue,
    Highlighted,
    Identifier,
    Keyword,
    Literal,
    MethodValue,
    NewLine,
    Indent,
    ParameterValue,
    ScenarioValue,
    BlankLine,
    StringLiteral,
    Word,
    HighlightedIdentifier;

    fun asCss(): String = "token-${name.unCamelToSeparated()}"
}