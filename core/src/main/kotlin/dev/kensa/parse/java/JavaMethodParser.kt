package dev.kensa.parse.java

import dev.kensa.Configuration
import dev.kensa.parse.*

class JavaMethodParser(
    isClassTest: (Java20Parser.MethodDeclarationContext) -> Boolean,
    isInterfaceTest: (Java20Parser.InterfaceMethodDeclarationContext) -> Boolean,
    override val configuration: Configuration
) : MethodParser,
    ParserCache by RealParserCache(),
    ParserDelegate by JavaParserDelegate(isClassTest, isInterfaceTest, configuration) {

    override val toSimpleTypeName: (Class<*>) -> String = { it.simpleName }
}