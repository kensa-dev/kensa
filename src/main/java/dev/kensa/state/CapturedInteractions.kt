package dev.kensa.state

import dev.kensa.state.SetupStrategy.*
import dev.kensa.util.Attributes
import dev.kensa.util.Attributes.Companion.emptyAttributes
import dev.kensa.util.Attributes.Key.Group
import dev.kensa.util.KensaMap

enum class SetupStrategy {
    Grouped,
    Ignored,
    Ungrouped,
}

class CapturedInteractions : KensaMap<CapturedInteractions>() {

    var isUnderTestEnabled = true

    var isUnderTest: Boolean = false
        set(value) {
            field = value
            isSetup = false
        }

    var setupStrategy: SetupStrategy = Ungrouped
    private var isSetup = true

    fun capture(builder: CapturedInteractionBuilder) {
        if (isSetup) {
            if(setupStrategy == Ignored) return
            if (setupStrategy == Grouped) {
                builder.group("Setup")
            }
        }

        with(builder) {
            if (isUnderTestEnabled) {
                underTest(isUnderTest)
            }
            applyTo(this@CapturedInteractions)
        }
    }

    fun captureTimePassing(message: String = "Some Time Later") {
        put(sdMarkerKey, "...$message...", attributes = if (isUnderTest) Attributes.of(Group, "Test") else emptyAttributes())
    }

    fun divider(message: String = "") {
        put(sdMarkerKey, "==$message==", attributes = if (isUnderTest) Attributes.of(Group, "Test") else emptyAttributes())
    }

    fun containsEntriesMatching(predicate: (Entry) -> Boolean): Boolean = entrySet().any(predicate)

    companion object {
        const val sdMarkerKey = "SD-MARKER"
    }
}