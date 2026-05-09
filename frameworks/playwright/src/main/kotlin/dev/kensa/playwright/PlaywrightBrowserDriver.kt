package dev.kensa.playwright

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import dev.kensa.uitesting.BrowserDriver

/**
 * [BrowserDriver] implementation backed by a Playwright [Page].
 *
 * Example:
 * ```kotlin
 * override fun createDriver(): BrowserDriver {
 *     val playwright = Playwright.create()
 *     val browser = playwright.chromium().launch(BrowserType.LaunchOptions().setHeadless(true))
 *     return PlaywrightBrowserDriver(browser.newPage(), playwright)
 * }
 * ```
 */
class PlaywrightBrowserDriver(
    val page: Page,
    private val playwright: Playwright,
) : BrowserDriver {

    override fun takeScreenshot(): ByteArray =
        page.screenshot(Page.ScreenshotOptions().setFullPage(true))

    override fun quit() {
        val context = page.context()
        val browser = context.browser()
        val closers = listOfNotNull(
            context::close,
            browser?.let { { it.close() } },
            playwright::close,
        )

        var primary: Throwable? = null
        for (close in closers) {
            try {
                close()
            } catch (t: Throwable) {
                if (primary == null) primary = t else primary.addSuppressed(t)
            }
        }
        primary?.let { throw it }
    }
}
