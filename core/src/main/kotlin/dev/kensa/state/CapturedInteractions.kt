package dev.kensa.state

import dev.kensa.state.SetupStrategy.Grouped
import dev.kensa.state.SetupStrategy.Ignored
import dev.kensa.util.Attributes
import dev.kensa.util.Attributes.Companion.emptyAttributes
import dev.kensa.util.Attributes.Key.Group
import dev.kensa.util.KensaMap

/**
 * Defines how setup interactions are rendered in the Sequence Diagram.
 */
enum class SetupStrategy {
    /**
     * Setup interactions are grouped into separate boxes from the main interactions under test.
     */
    Grouped,

    /**
     * Setup interactions are not displayed in the Sequence Diagram.
     */
    Ignored,

    /**
     * Setup interactions are displayed but not grouped into a box.
     */
    Ungrouped,
}

class CapturedInteractions(private val setupStrategy: SetupStrategy) : KensaMap<CapturedInteractions>() {

    var isUnderTestEnabled = true

    var isUnderTest: Boolean = false
        set(value) {
            field = value
            isSetup = false
        }

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