package dev.kensa.example

import dev.kensa.ExpandableSentence
import dev.kensa.RenderedValue

class KotlinWithIgnoreHint {

    @RenderedValue
    private val trackingId = "TRK-001"

    fun ignoresSingleStatement() {
        /*+ Ignore */
        ignoredHelper()
        given {}
    }

    fun ignoresTwoStatements() {
        /*+ Ignore:2 */
        ignoredHelper()
        ignoredHelper()
        given {}
    }

    fun rendersValueBelowIgnoredLine() {
        /*+ Ignore */
        ignoredHelper()
        theTrackingIdIs(trackingId)
    }

    @ExpandableSentence
    fun blockBodyExpandable() = arrayOf(
        /*+ Ignore */
        ignoredMatcher(),
        aFirstName of "John",
        aLastName of "Smith",
    )

    @RenderedValue
    private val aFirstName = "John"

    @RenderedValue
    private val aLastName = "Smith"

    private infix fun String.of(value: String): Matcher = Matcher()
    private fun ignoredMatcher(): Matcher = Matcher()
    private fun ignoredHelper() = Unit
    private fun theTrackingIdIs(value: String) = Unit
    private fun given(block: () -> Unit = {}) {}

    class Matcher
}
