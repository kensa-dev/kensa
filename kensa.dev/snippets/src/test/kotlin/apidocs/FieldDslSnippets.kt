// Snippet source for kensa.dev/docs/field-assertion-dsl/xml.mdx and json.mdx — key API shapes
package apidocs

import dev.kensa.kotest.testsupport.field.matching
import dev.kensa.kotest.testsupport.field.of
import dev.kensa.kotest.testsupport.field.withListOf
import dev.kensa.kotest.testsupport.field.xml.XmlField
import dev.kensa.kotest.testsupport.field.xml.XmlListField
import dev.kensa.kotest.testsupport.field.xml.XmlStringField
import dev.kensa.kotest.testsupport.field.xml.XmlTextField
import dev.kensa.kotest.testsupport.field.xml.compileXPath
import io.kotest.matchers.and
import io.kotest.matchers.collections.containExactlyInAnyOrder

enum class Provider {
    FIBREVISION;

    companion object {
        fun fromString(s: String): Provider = FIBREVISION
    }
}

// Path-string form
val aProviderByPath = XmlField<Provider>("/order/@provider") {
    Provider.fromString(it.nodeValue)
}

// Compiled-expression form
val aProvider = XmlField(
    compileXPath("/order/@provider"),
    "Provider"
) { node -> Provider.fromString(node.nodeValue) }

val anAmount = XmlTextField(compileXPath("/order/amount"), "Amount") { it.toBigDecimal() }

val status = XmlStringField("/FeasibilityResponse/Status")
val customerName = XmlStringField("/order/customer/name", "Customer Name")

val profileTypes = XmlListField(
    compileXPath("/FeasibilityResponse/Profiles/Profile/Type"),
    "Profile Types"
) { it.textContent }

val orderIdExpr = compileXPath("/order/@id")

// composition used in the worked example
val composed = (status of "SERVICEABLE")
    .and(customerName of "Alice")
    .and(profileTypes matching containExactlyInAnyOrder("FTTC"))
    .and(profileTypes.withListOf("FTTC"))
