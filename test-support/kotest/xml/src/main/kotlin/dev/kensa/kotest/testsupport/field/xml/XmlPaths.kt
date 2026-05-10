package dev.kensa.kotest.testsupport.field.xml

import javax.xml.xpath.XPathExpression
import javax.xml.xpath.XPathFactory

private val xPathFactory = XPathFactory.newInstance()
private val xPathLock = Any()

/**
 * Thread-safely compiles [path] and wraps the result in an [XPathExpressionWrapper] so the
 * original path string is retained for tooling (e.g. report rendering).
 *
 * Used by the path-string constructors on [XmlField] and friends. Public so callers can compile
 * once and pass the result to constructors taking [XPathExpression] directly.
 *
 * For namespace-aware compilation, build your own [XPathExpression] using `XPathFactory` with a
 * `NamespaceContext` and pass that to the existing constructors instead.
 */
fun compileXPath(path: String): XPathExpression = synchronized(xPathLock) {
    XPathExpressionWrapper(xPathFactory.newXPath().compile(path), path)
}
