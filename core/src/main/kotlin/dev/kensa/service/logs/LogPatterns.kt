package dev.kensa.service.logs

/**
 * Helper factory methods for creating log parsing patterns.
 *
 * Conventions:
 * - All returned regexes are anchored (match the whole line).
 * - Capturing group 1 is the extracted identifier value.
 */
object LogPatterns {

    /**
     * Matches a block delimiter line that starts with [delimiterLine], allowing optional leading whitespace
     * and any trailing suffix (timestamps, extra markers, etc.).
     *
     * Example matches:
     * - "***********"
     * - "   *********** 2026-02-02T12:34:56Z"
     */
    fun delimiterPrefix(delimiterLine: String): Regex =
        Regex("^\\s*${Regex.escape(delimiterLine.trim())}.*$")

    /**
     * Builds an identifier-extraction pattern for lines like:
     *
     * - "TrackingId: <value>"
     * - "TrackingId = <value>"
     * - "TrackingId -> <value>"
     *
     * Group 1 captures the value portion.
     *
     * @param fieldName The identifier field name to match (literal).
     * @param separators Allowed separators between field and value. Defaults to ":".
     */
    fun idField(
        fieldName: String,
        separators: List<String> = listOf(":")
    ): Regex {
        require(separators.isNotEmpty()) { "separators must not be empty" }

        val sepAlternation = separators.joinToString("|") { Regex.escape(it) }

        return Regex(
            pattern = "^\\s*${Regex.escape(fieldName.trim())}\\s*(?:$sepAlternation)\\s*(.+?)\\s*$"
        )
    }

    /**
     * Builds a more permissive identifier-extraction pattern where the separator is any non-word
     * punctuation between field and value. Useful when teams have inconsistent separators.
     *
     * Examples:
     * - "TrackingId: abc"
     * - "TrackingId = abc"
     * - "TrackingId -> abc"
     *
     * Group 1 captures the value portion.
     */
    fun idFieldAnySeparator(fieldName: String): Regex =
        Regex("^\\s*${Regex.escape(fieldName.trim())}\\s*[^\\w\\s]+\\s*(.+?)\\s*$")
}