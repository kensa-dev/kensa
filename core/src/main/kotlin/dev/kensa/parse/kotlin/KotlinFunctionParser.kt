package dev.kensa.parse.kotlin

import dev.kensa.Configuration
import dev.kensa.parse.*
import org.antlr.v4.runtime.atn.PredictionMode

class KotlinFunctionParser(isTest: (KotlinParser.FunctionDeclarationContext) -> Boolean, override val configuration: Configuration, private val antlrErrorListenerDisabled: Boolean, private val antlrPredicationMode: PredictionMode) : MethodParser,
    ParserCache by RealParserCache(),
    ParserDelegate by KotlinParserDelegate(isTest, antlrErrorListenerDisabled, antlrPredicationMode) {

    override val toSimpleTypeName: (Class<*>) -> String = { it.kotlin.simpleName ?: throw IllegalArgumentException("Parameter types must have names") }
}