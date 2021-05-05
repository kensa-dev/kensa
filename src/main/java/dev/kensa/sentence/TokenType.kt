package dev.kensa.sentence

import dev.kensa.util.Strings.unCamelToSeparated

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
    Word;

    fun asCss(): String = "token-${unCamelToSeparated(name)}"
}