package dev.kensa.parse.kotlin

import dev.kensa.Configuration
import dev.kensa.parse.*

class KotlinFunctionParser(isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean, override val configuration: Configuration) : MethodParser,
    ParserCache by RealParserCache(),
    ParserDelegate by KotlinParserDelegate(isTest, configuration) {

    override val toSimpleTypeName: (Class<*>) -> String = { it.kotlin.simpleName ?: throw IllegalArgumentException("Parameter types must have names") }
}