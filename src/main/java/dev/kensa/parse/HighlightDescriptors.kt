package dev.kensa.parse

import dev.kensa.MaxHighlightColoursBehaviour
import dev.kensa.MaxHighlightColoursBehaviour.RollOver
import dev.kensa.MaxHighlightColoursBehaviour.Stop
import dev.kensa.util.NamedValue

/**
 * The test spec will often include explicit references (by name) to fields or parameters
 * that have the @Highlight or @SentenceValue annotation.
 *
 * This class ties each named value that is referenced in the test to a highlight colour index. This
 * colour index can be used in the UI to quickly distinguish these different highlighted
 * values.
 *
 * We may also find the _values_ of those highlighted fields/parameters cropping up in
 * captured interactions without that direct link to the value's original field/parameter "source".
 * Where such a value is matches only one highlighted field/parameter, we can use the pre-assigned
 * colour index for that field/parameter; if it matches more than one field/parameter then we
 * have to label that value with an "ambiguous" colour index.
 */
class HighlightDescriptors private constructor(
    private val namedValues: List<NamedValue>,
    private val maxHighlightColours: Int,
    private val maxHighlightColoursBehaviour: MaxHighlightColoursBehaviour
) {
    /**
     * This map will be populated gradually, as each non-ambiguous field/value is encountered
     * in the test spec, so that colour indexes are assigned according to the order in which
     * the highlighted items are _referenced_ in the test body, rather than the order in
     * which the `@Highlight` fields/params/methods are _declared in the test class.
     * This addresses the case where there may be a large number of `@Highlight` fields
     * defined in a superclass, but only a small subset of these is ever used in each test.
     */
    private val alreadyReferencedDescriptorsByName = mutableMapOf<String, HighlightDescriptor>()

    val descriptors: Collection<HighlightDescriptor>
        get() = alreadyReferencedDescriptorsByName.values.plus(nonReferencedDescriptors())

    fun highlightDescriptorFor(value: Any, identifier: String? = null): HighlightDescriptor? =
        identifier
            ?.let { findByIdentifier(it) }
            ?: findByValue(value)

    private fun findByIdentifier(identifier: String): HighlightDescriptor? =
        alreadyReferencedDescriptorsByName[identifier]
        ?: createNewDescriptorIfNecessaryForIdentifier(identifier)

    private fun findByValue(value: Any): HighlightDescriptor? =
        namedValues
            .filter { it.value == value }
            .let { matchingNamedValues ->
                when (matchingNamedValues.size) {
                    0 -> null
                    1 -> findByIdentifier(matchingNamedValues[0].name)
                    else -> ambiguousDescriptor(matchingNamedValues)
                }
            }

    private fun createNewDescriptorIfNecessaryForIdentifier(identifier: String): HighlightDescriptor? =
        namedValues
            .singleOrNull { it.name == identifier }
            ?.let { newDescriptorFor(it) }
            ?.also { alreadyReferencedDescriptorsByName[it.name] = it }

    private fun newDescriptorFor(namedValue: NamedValue): HighlightDescriptor =
        HighlightDescriptor(
            namedValue,
            colourIndex = nextColourIndex()
        )

    private fun ambiguousDescriptor(matches: List<NamedValue>): HighlightDescriptor =
        HighlightDescriptor(
            value = matches.first().value,
            name = matches.joinToString(" or ") { "'${it.name}'" }.plus(" (ambiguous)"),
            colourIndex = HighlightDescriptor.AMBIGUOUS_COLOUR_INDEX
        )

    private fun nonReferencedDescriptors() = namedValues
        .filter { !alreadyReferencedDescriptorsByName.containsKey(it.name) }
        .mapIndexed { index, namedValue ->
            HighlightDescriptor(
                namedValue,
                colourIndex = nextColourIndex(index)
            )
        }

    private fun nextColourIndex(additionalIndexToAdd: Int = 0) : String =
        alreadyReferencedDescriptorsByName.size
            .plus(additionalIndexToAdd)
            .let {
                when (maxHighlightColoursBehaviour) {
                    RollOver -> "${(it % maxHighlightColours) + 1}"
                    Stop -> if (it >= maxHighlightColours) HighlightDescriptor.NO_COLOUR_INDEX else "${it + 1}"
                }
            }

    companion object {
        fun from(namedValues: List<NamedValue>, maxHighlightColours: Int, maxHighlightColoursBehaviour: MaxHighlightColoursBehaviour) =
            HighlightDescriptors(namedValues, maxHighlightColours, maxHighlightColoursBehaviour)
    }
}