package dev.kensa.hamkrest.testsupport.field.xml

import org.w3c.dom.Node
import org.w3c.dom.NodeList
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathExpression

/** Returns the single [Node] matched by [path], or `null` if nothing matches. */
fun Node.getNodeOrNull(path: XPathExpression): Node? = path.evaluate(this, XPathConstants.NODE) as? Node

/** Returns all [Node]s matched by [path] in document order. */
fun Node.getNodes(path: XPathExpression): List<Node> = (path.evaluate(this, XPathConstants.NODESET) as NodeList).toList()

private fun NodeList.toList(): List<Node> = (0 until length).map { item(it) }
