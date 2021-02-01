package dev.kensa.parse.java

import dev.kensa.parse.Java8Parser
import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParserCache
import dev.kensa.parse.ParserDelegate

class JavaMethodParser : MethodParser<Java8Parser.MethodDeclarationContext>,
    ParserCache<Java8Parser.MethodDeclarationContext> by JavaParserCache(),
    ParserDelegate<Java8Parser.MethodDeclarationContext> by JavaParserDelegate {

    override val toSimpleTypeName: (Class<*>) -> String = { it.simpleName }
}