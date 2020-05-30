package dev.kensa.state

import dev.kensa.util.KensaMap
import java.util.function.Predicate

class CapturedInteractions : KensaMap<CapturedInteractions>() {

    private var isUnderTestEnabled = true
    private var isUnderTest = false

    fun capture(builder: CapturedInteractionBuilder) {
        if (isUnderTestEnabled) {
            builder.underTest(isUnderTest)
        }
        builder.applyTo(this)
    }

    fun setUnderTest(isUnderTest: Boolean) {
        this.isUnderTest = isUnderTest
    }

    fun disableUnderTest() {
        isUnderTestEnabled = false
    }

    fun containsEntriesMatching(predicate: Predicate<Entry>): Boolean = entrySet().stream().anyMatch(predicate)
}