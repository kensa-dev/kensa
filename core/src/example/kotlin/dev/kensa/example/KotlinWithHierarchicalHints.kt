package dev.kensa.example

import dev.kensa.RenderedHintStrategy.HintFromProperty
import dev.kensa.RenderedValueStrategy.UseIdentifierName
import dev.kensa.RenderedValueWithHint

sealed class PathExpr(val path: String) {
    infix fun of(value: String) = this
}

class JsonPathExpr(path: String) : PathExpr(path)
class XPathExpr(path: String, val xmlns: String = "") : PathExpr(path)

@RenderedValueWithHint(type = PathExpr::class, valueStrategy = UseIdentifierName, hintParam = "path", hintStrategy = HintFromProperty)
@RenderedValueWithHint(type = XPathExpr::class, valueStrategy = UseIdentifierName, hintParam = "xmlns", hintStrategy = HintFromProperty)
class KotlinWithHierarchicalHints {

    fun simpleTest() {
        aJsonPath of "expected"
        anXPath of "expected"
    }

    val aJsonPath = JsonPathExpr("$.a.b")
    val anXPath = XPathExpr("/root/node", xmlns = "ns:example")
}
