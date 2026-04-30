package dev.kensa.uitesting

/**
 * Driver-agnostic abstraction over a browser automation library (Selenium, Playwright, etc.).
 *
 * Implementations provide a minimal cross-driver API ([takeScreenshot], [quit]) plus a typed
 * accessor for the underlying driver instance (e.g. `PlaywrightBrowserDriver.page`,
 * `SeleniumBrowserDriver.webDriver`). [UserStub] subclasses use the typed accessor to call
 * driver-specific APIs in their page-object methods.
 */
interface BrowserDriver {
    /** Capture the current browser viewport as a PNG byte array. */
    fun takeScreenshot(): ByteArray

    /** Quit / close the browser session. */
    fun quit()
}
