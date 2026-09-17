package dev.kensa.example

import dev.kensa.Action
import dev.kensa.ActionContext
import dev.kensa.ExpandableRenderedValue
import dev.kensa.GivensContext
import org.junit.jupiter.api.Test

class KotlinWithExpandableRenderedValueTest : KotlinExampleTest() {

    @Test
    fun rendersAnExpandableValueWithNoArguments() {
        given(aDispatchedShipment())

        whenever(theCourierReports(theObservedLifecycle()))
    }

    @Test
    fun rendersAnExpandableValueWithArguments() {
        given(aDispatchedShipment())

        whenever(theCourierReports(theObservedLifecycleFor("TRK-99")))
    }

    @ExpandableRenderedValue
    private fun theObservedLifecycle(): List<String> = listOf("Acknowledged", "Committed", "Dispatched")

    @ExpandableRenderedValue
    private fun theObservedLifecycleFor(trackingNumber: String): List<String> =
        listOf("Acknowledged $trackingNumber", "Committed $trackingNumber", "Dispatched $trackingNumber")

    private fun theCourierReports(lifecycle: List<String>) = Action<ActionContext> { }

    private fun aDispatchedShipment() = Action<GivensContext> { }
}
