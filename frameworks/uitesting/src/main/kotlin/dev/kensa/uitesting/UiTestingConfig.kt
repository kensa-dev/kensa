package dev.kensa.uitesting

import dev.kensa.Configuration
import dev.kensa.computeExtraIfAbsent

/**
 * Per-Configuration UI testing settings, registered as a [Configuration] extra and accessed via the
 * [Configuration.uiTesting] extension property:
 *
 * ```
 * Kensa.konfigure {
 *     uiTesting.autoScreenshotOnFailure = true
 * }
 * ```
 */
class UiTestingConfig {
    var autoScreenshotOnFailure: Boolean = false
}

val Configuration.uiTesting: UiTestingConfig
    get() = computeExtraIfAbsent { UiTestingConfig() }
