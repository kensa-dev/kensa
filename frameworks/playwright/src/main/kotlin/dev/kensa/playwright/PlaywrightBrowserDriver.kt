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
        try {
            context.close()
        } finally {
            try {
                browser?.close()
            } finally {
                playwright.close()
            }
        }
    }
}
