package dev.kensa.example

import dev.kensa.RenderedValue

class KotlinWithReplaceSentence {

    @RenderedValue
    private val trackingId = "TRK-001"

    fun replacedWithPlainWords() {
        /*+ ReplaceSentence: given a simple replacement */
        given {}
    }

    fun replacedWithFieldInterpolation() {
        /*+ ReplaceSentence: given the tracking id is {trackingId} */
        given {}
    }

    fun replacedWithFixturesInterpolation() {
        /*+ ReplaceSentence: given the order {fixtures[trackingId]} is pending */
        given {}
    }

    fun replacedWithChainedFixturesInterpolation() {
        /*+ ReplaceSentence: given the order {fixtures[trackingId].value} is pending */
        given {}
    }

    fun replacedAndSubsequentStatementsUnaffected() {
        /*+ ReplaceSentence: given OpenNetwork will complete the order {fixtures[trackingId]} */
        given {}
        whenever { doSomething() }
    }

    fun replacedWithOutputsByKeyInterpolation() {
        /*+ ReplaceSentence: given the result {outputs("trackingKey").value} is received */
        given {}
    }

    fun replacedWithOutputsByNameInterpolation() {
        /*+ ReplaceSentence: given the result {outputs[trackingName].value} is received */
        given {}
    }

    fun replacedWithMultipleInterpolations() {
        /*+ ReplaceSentence: given {trackingId} and {fixtures[orderId]} are both valid */
        given {}
    }

    fun replacedMultipleStatements() {
        /*+ ReplaceSentence: given the network will complete the order */
        given {}
        /*+ ReplaceSentence: then the result is confirmed */
        then {}
    }

    private fun given(block: () -> Unit = {}) {}
    private fun whenever(block: () -> Unit = {}) {}
    private fun then(block: () -> Unit = {}) {}
    private fun doSomething() {}
}
