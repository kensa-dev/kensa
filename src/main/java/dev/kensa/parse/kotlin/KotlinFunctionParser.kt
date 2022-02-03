package dev.kensa.parse.kotlin

import dev.kensa.parse.*
import java.lang.reflect.Method
import kotlin.reflect.jvm.kotlinFunction

class KotlinFunctionParser : MethodParser<KotlinParser.FunctionDeclarationContext>,
    ParserCache<KotlinParser.FunctionDeclarationContext> by KotlinParserCache(),
    ParserDelegate<KotlinParser.FunctionDeclarationContext> by KotlinParserDelegate {

    override val toSimpleTypeName: (Class<*>) -> String = { it.kotlin.simpleName ?: throw IllegalArgumentException("Parameter types must have names") }

    override fun realNameOf(method: Method) : String = method.kotlinFunction?.name ?: throw IllegalStateException("Unable to convert method [${method.name}] to a Kotlin function")
}