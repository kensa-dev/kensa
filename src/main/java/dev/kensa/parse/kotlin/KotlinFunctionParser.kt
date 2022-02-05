package dev.kensa.parse.kotlin

import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParserCache
import dev.kensa.parse.ParserDelegate
import dev.kensa.parse.RealParserCache

class KotlinFunctionParser : MethodParser,
    ParserCache by RealParserCache(),
    ParserDelegate by KotlinParserDelegate {

    override val toSimpleTypeName: (Class<*>) -> String = { it.kotlin.simpleName ?: throw IllegalArgumentException("Parameter types must have names") }
}