package dev.kensa.kotest.testsupport.field.xml

import org.xml.sax.InputSource
import javax.xml.namespace.QName
import javax.xml.xpath.XPathExpression

/**
 * An [XPathExpression] that retains its original [path] string, so consumers (e.g. report
 * rendering) can introspect it via [XmlField.path].
 *
 * Wrap compiled XPath expressions with this class when constructing [XmlField]s, so the path is
 * preserved for tooling.
 */
class XPathExpressionWrapper(private val xPathExpression: XPathExpression, val path: String) : XPathExpression {
    override fun evaluate(item: Any, returnType: QName): Any? = xPathExpression.evaluate(item, returnType)
    override fun evaluate(item: Any): String? = xPathExpression.evaluate(item)
    override fun evaluate(source: InputSource, returnType: QName): Any? = xPathExpression.evaluate(source, returnType)
    override fun evaluate(source: InputSource): String? = xPathExpression.evaluate(source)
}
