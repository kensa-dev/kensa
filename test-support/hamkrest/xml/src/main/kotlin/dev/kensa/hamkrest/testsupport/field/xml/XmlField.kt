package dev.kensa.hamkrest.testsupport.field.xml

import dev.kensa.hamkrest.testsupport.field.MatcherField
import org.w3c.dom.Node
import javax.xml.xpath.XPathExpression

/**
 * A [MatcherField] backed by an XPath expression evaluated against a DOM [Node].
 *
 * Wrap [XPathExpression]s in [XPathExpressionWrapper] so [path] is introspectable by tooling
 * (e.g. for HTML report rendering).
 *
 * ```
 * val aProvider = XmlField(compile("/order/@provider"), "Provider") { Provider.fromString(it.nodeValue) }
 * ```
 */
open class XmlField<T>(
    expressionProvider: () -> XPathExpression,
    name: String?,
    private val transform: (Node) -> T?,
) : MatcherField<Node, T> {
    constructor(expression: XPathExpression, transform: (Node) -> T?) : this({ expression }, null, transform)
    constructor(expression: XPathExpression, name: String, transform: (Node) -> T?) : this({ expression }, name, transform)
    constructor(expressionProvider: () -> XPathExpression, transform: (Node) -> T?) : this(expressionProvider, null, transform)

    private val expression by lazy(expressionProvider)

    override val name: String = name ?: this::class.simpleName!!

    /** XPath path string when constructed from an [XPathExpressionWrapper]; otherwise `null`. */
    val path: String? get() = (expression as? XPathExpressionWrapper)?.path

    override fun extract(value: Node): T? = value.getNodeOrNull(expression)?.let(transform)
}

/**
 * An [XmlField] that extracts the `textContent` of the matched node and applies [transform].
 *
 * For the common identity-transform case, prefer [XmlStringField].
 */
open class XmlTextField<T>(
    expressionProvider: () -> XPathExpression,
    name: String?,
    transform: (String) -> T?,
) : XmlField<T>(expressionProvider, name, { it.textContent?.let(transform) }) {
    constructor(expression: XPathExpression, transform: (String) -> T?) : this({ expression }, null, transform)
    constructor(expression: XPathExpression, name: String, transform: (String) -> T?) : this({ expression }, name, transform)
}

/**
 * An [XmlTextField] that returns the matched node's `textContent` verbatim.
 *
 * ```
 * val aCustomerName = XmlStringField(compile("/order/customer/name"), "Customer Name")
 * ```
 */
open class XmlStringField(
    expressionProvider: () -> XPathExpression,
    name: String? = null,
) : XmlTextField<String>(expressionProvider, name, { it }) {
    constructor(expression: XPathExpression, name: String? = null) : this({ expression }, name)
}

/** An [XmlField] that returns the matched [Node] itself. */
open class XmlNodeField(
    expressionProvider: () -> XPathExpression,
    name: String? = null,
) : XmlField<Node>(expressionProvider, name, { it }) {
    constructor(expression: XPathExpression, name: String? = null) : this({ expression }, name)
}

/**
 * A [MatcherField] that extracts an ordered [List] of values, applying [transform] to each
 * matched node.
 */
open class XmlListField<T>(
    expressionProvider: () -> XPathExpression,
    name: String?,
    private val transform: (Node) -> T,
) : MatcherField<Node, List<T>> {
    constructor(expression: XPathExpression, transform: (Node) -> T) : this({ expression }, null, transform)
    constructor(expression: XPathExpression, name: String, transform: (Node) -> T) : this({ expression }, name, transform)
    constructor(expressionProvider: () -> XPathExpression, transform: (Node) -> T) : this(expressionProvider, null, transform)

    private val expression by lazy(expressionProvider)
    override val name: String = name ?: this::class.simpleName!!

    /** XPath path string when constructed from an [XPathExpressionWrapper]; otherwise `null`. */
    val path: String? get() = (expression as? XPathExpressionWrapper)?.path

    override fun extract(value: Node): List<T> = value.getNodes(expression).map(transform)
}

/**
 * A [MatcherField] that extracts a [Set] of values (deduplicated), applying [transform] to each
 * matched node.
 */
open class XmlSetField<T>(
    expressionProvider: () -> XPathExpression,
    name: String?,
    private val transform: (Node) -> T,
) : MatcherField<Node, Set<T>> {
    constructor(expressionProvider: () -> XPathExpression, transform: (Node) -> T) : this(expressionProvider, null, transform)
    constructor(expression: XPathExpression, transform: (Node) -> T) : this({ expression }, transform)
    constructor(expression: XPathExpression, name: String, transform: (Node) -> T) : this({ expression }, name, transform)

    private val expression by lazy(expressionProvider)
    override val name: String = name ?: this::class.simpleName!!

    /** XPath path string when constructed from an [XPathExpressionWrapper]; otherwise `null`. */
    val path: String? get() = (expression as? XPathExpressionWrapper)?.path

    override fun extract(value: Node): Set<T> = value.getNodes(expression).map(transform).toSet()
}
