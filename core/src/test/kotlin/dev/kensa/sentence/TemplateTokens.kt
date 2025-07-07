package dev.kensa.sentence

import dev.kensa.parse.EmphasisDescriptor


fun TemplateToken.Type.asTemplateToken(template: String = "", emphasis: EmphasisDescriptor = EmphasisDescriptor.Default, vararg types: TemplateToken.Type) = TemplateToken.SimpleTemplateToken(template, emphasis, setOf(this, *types))

fun aRenderedValueOf(value: String, cssClasses: Set<String> = emptySet()) = RenderedToken.RenderedValueToken(value, cssClasses)

//object TemplateTokens {
//
//    fun aWordOf(value: String, emphasis: EmphasisDescriptor = EmphasisDescriptor.Companion.Default) = TemplateToken.SimpleTemplateToken(value, types = setOf(Word), emphasis = emphasis)
//
//    fun aNumberLiteralOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(NumberLiteral))
//
//    fun aBooleanLiteralOf(value: Boolean) = TemplateToken.SimpleTemplateToken(value.toString(), types = setOf(BooleanLiteral))
//
//    fun aCharacterLiteralOf(value: Char) = TemplateToken.SimpleTemplateToken(value.toString(), types = setOf(CharacterLiteral))
//
//    fun aNullLiteral() = TemplateToken.SimpleTemplateToken("null", types = setOf(NullLiteral))
//
//    fun aProtectedPhraseOf(value: String, emphasis: EmphasisDescriptor): TemplateToken = TemplateToken.SimpleTemplateToken(template = value, types = setOf(ProtectedPhrase), emphasis = emphasis)
//
//    fun aNewline() = TemplateToken.SimpleTemplateToken("", types = setOf(NewLine))
//
//    fun anIndent() = TemplateToken.SimpleTemplateToken("", types = setOf(Indent))
//
//    fun aStringLiteralOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(StringLiteral))
//
//    fun aStringLiteralAcronymOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(StringLiteral, Acronym))
//
//    fun anAcronymOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(Acronym))
//
//    fun aKeywordOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(Keyword))
//
//    fun anIdentifierOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(Identifier))
//
//    fun aFixturesValueOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(FixturesValue))
//
//    fun aFieldValueOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(FieldValue))
//
//    fun aMethodValueOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(MethodValue))
//
//    fun aParameterValueOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(ParameterValue))
//
//    fun anOperatorOf(value: String) = TemplateToken.SimpleTemplateToken(value, types = setOf(Operator))
//}