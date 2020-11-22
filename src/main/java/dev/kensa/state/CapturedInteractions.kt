package dev.kensa.state

import dev.kensa.util.KensaMap
import java.util.function.Predicate

class CapturedInteractions : KensaMap<CapturedInteractions>() {

    var isUnderTestEnabled = true
    var isUnderTest = false

    fun capture(builder: CapturedInteractionBuilder) {
        with(builder) {
            if (isUnderTestEnabled) {
                underTest(isUnderTest)
            }
            applyTo(this@CapturedInteractions)
        }
    }

    fun disableUnderTest() {
        isUnderTestEnabled = false
    }

    fun containsEntriesMatching(predicate: Predicate<Entry>): Boolean = entrySet().stream().anyMatch(predicate)
}