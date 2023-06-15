package dev.kensa.state

import dev.kensa.util.Attributes
import dev.kensa.util.Attributes.Companion.emptyAttributes
import dev.kensa.util.Attributes.Key.Group
import dev.kensa.util.KensaMap

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

    fun captureTimePassing(message: String = "Some Time Later") {
        put(sdMarkerKey, "...$message...", if (isUnderTest) Attributes.of(Group, "Test") else emptyAttributes())
    }

    fun divider(message: String = "") {
        put(sdMarkerKey, "==$message==", if (isUnderTest) Attributes.of(Group, "Test") else emptyAttributes())
    }

    fun disableUnderTest() {
        isUnderTestEnabled = false
    }

    fun containsEntriesMatching(predicate: (Entry) -> Boolean): Boolean = entrySet().any(predicate)

    companion object {
        const val sdMarkerKey = "SD-MARKER"
    }
}