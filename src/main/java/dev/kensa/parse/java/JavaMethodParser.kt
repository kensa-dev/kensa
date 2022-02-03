package dev.kensa.parse.java

import dev.kensa.parse.*
import java.lang.reflect.Method

class JavaMethodParser : MethodParser<Java8Parser.MethodDeclarationContext>,
    ParserCache<Java8Parser.MethodDeclarationContext> by JavaParserCache(),
    ParserDelegate<Java8Parser.MethodDeclarationContext> by JavaParserDelegate {

    override val toSimpleTypeName: (Class<*>) -> String = { it.simpleName }

    override fun realNameOf(method: Method): String = method.name
}