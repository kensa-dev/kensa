package dev.kensa.output.json

import dev.kensa.state.CapturedInteractions.Companion.sdMarkerKey
import dev.kensa.util.KensaMap

/**
 * Interaction keys are built by `CapturedInteractionBuilder` as
 * `"<descriptor> from <fromParty> to <toParty>"`, so both the report JSON and the
 * index metrics pull the two party names out with this one pattern.
 */
internal val interactionKeyPattern = "^(.*) from (.+) to (.+)$".toRegex()

private val bracketedMarkerPattern = "^\\{.+}.*$".toRegex()

/**
 * Dividers, time-passing notes and the sequence diagram marker live in the same map as real
 * interactions but are not rendered as messages, so neither the report nor the index counts them.
 */
internal fun KensaMap.Entry.isRenderedInteraction(): Boolean =
    key != sdMarkerKey && !key.matches(bracketedMarkerPattern)
