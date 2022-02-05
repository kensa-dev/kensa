package dev.kensa.parse.java

import dev.kensa.parse.*

class JavaMethodParser : MethodParser,
    ParserCache by RealParserCache(),
    ParserDelegate by JavaParserDelegate {

    override val toSimpleTypeName: (Class<*>) -> String = { it.simpleName }
}