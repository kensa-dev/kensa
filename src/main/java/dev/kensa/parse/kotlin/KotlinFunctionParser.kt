package dev.kensa.parse.kotlin

import dev.kensa.parse.KotlinParser
import dev.kensa.parse.MethodParser
import dev.kensa.parse.ParserCache
import dev.kensa.parse.ParserDelegate

class KotlinFunctionParser : MethodParser<KotlinParser.FunctionDeclarationContext>,
        ParserCache<KotlinParser.FunctionDeclarationContext> by KotlinParserCache(),
        ParserDelegate<KotlinParser.FunctionDeclarationContext> by KotlinParserDelegate