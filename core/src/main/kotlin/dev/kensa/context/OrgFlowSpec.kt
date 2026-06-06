package dev.kensa.context

import dev.kensa.OrgFlow
import dev.kensa.OrgFlowMarker
import java.lang.reflect.AnnotatedElement

/**
 * Metadata for the named org-wide business flow a test is the canonical slice of (Kura
 * flow-grouping). Implemented either by [SimpleOrgFlowSpec] (from the legacy [OrgFlow]
 * annotation) or by a consumer's enum referenced via an [OrgFlowMarker] annotation.
 *
 * @property flowName the flow's display name. Named `flowName` (not `name`) so an enum can
 *   implement this without clashing with `Enum.name`; it is emitted to results JSON as `name`.
 * @property attributes free-form descriptive facets (e.g. `product`, `channel`), emitted as a
 *   JSON object with sorted keys.
 */
interface OrgFlowSpec {
    val category: String
    val flowName: String
    val attributes: Map<String, String>
}

data class SimpleOrgFlowSpec(
    override val category: String,
    override val flowName: String,
    override val attributes: Map<String, String> = emptyMap(),
) : OrgFlowSpec

// An OrgFlowMarker annotation carries a single OrgFlowSpec-typed member by convention; if it
// declared more than one, the first (in unspecified reflection order) would be used.
fun orgFlowOf(element: AnnotatedElement): OrgFlowSpec? =
    element.annotations.firstNotNullOfOrNull { annotation ->
        annotation.takeIf { it.annotationClass.java.isAnnotationPresent(OrgFlowMarker::class.java) }
            ?.let { it.annotationClass.java.declaredMethods }
            ?.firstNotNullOfOrNull { member ->
                runCatching { member.invoke(annotation) }.getOrNull() as? OrgFlowSpec
            }
    } ?: element.getAnnotation(OrgFlow::class.java)?.let {
        SimpleOrgFlowSpec(it.category, it.name, productAttributes(it.product))
    }

private fun productAttributes(product: String): Map<String, String> =
    if (product.isBlank()) emptyMap() else mapOf("product" to product)
