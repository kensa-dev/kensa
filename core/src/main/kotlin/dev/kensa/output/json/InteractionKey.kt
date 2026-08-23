package dev.kensa.output.json

/**
 * Interaction keys are built by `CapturedInteractionBuilder` as
 * `"<descriptor> from <fromParty> to <toParty>"`, so both the report JSON and the
 * index metrics pull the two party names out with this one pattern.
 */
internal val interactionKeyPattern = "^(.*) from (.+) to (.+)$".toRegex()
