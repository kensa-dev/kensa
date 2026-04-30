package dev.kensa.uitesting

/**
 * Base class for user-facing workflow objects. Test authors subclass this and add
 * domain-specific methods. The underlying [BrowserDriver] is hidden from test code.
 *
 * Example:
 * ```kotlin
 * class TheUser(driver: BrowserDriver) : UserStub<BrowserDriver>(driver) {
 *     fun navigatesToLoginPage() { ... }
 *     fun submitsLoginForm(username: String) {
 *         // ...
 *         screenshot("after submit")
 *     }
 * }
 * ```
 */
abstract class UserStub<D : BrowserDriver>(protected val driver: D) {

    /**
     * Capture the current browser state and store it for display in the Screenshots tab.
     * @param label Human-readable label shown beneath the screenshot in the carousel.
     */
    fun screenshot(label: String = "") {
        CapturedScreenshots.current().capture(driver, label)
    }
}
