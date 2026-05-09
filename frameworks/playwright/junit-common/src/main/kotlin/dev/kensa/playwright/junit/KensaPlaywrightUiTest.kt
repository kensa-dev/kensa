package dev.kensa.playwright.junit

import com.microsoft.playwright.BrowserType
import com.microsoft.playwright.Playwright
import dev.kensa.playwright.PlaywrightBrowserDriver
import dev.kensa.uitesting.UserStub
import dev.kensa.uitesting.junit.KensaUiTest

/**
 * Base class for Kensa UI tests that use Playwright. Handles driver creation —
 * subclasses only need to provide the [UserStub].
 *
 * Defaults: headless Chromium. Override [configureLaunchOptions] to customise.
 *
 * Example (Kotlin):
 * ```kotlin
 * class LoginTest : KensaPlaywrightUiTest<TheUser>() {
 *     override fun createUser(driver: PlaywrightBrowserDriver) = TheUser(driver)
 *
 *     override fun configureLaunchOptions(options: BrowserType.LaunchOptions) {
 *         options.setHeadless(false).setSlowMo(100.0)
 *     }
 * }
 * ```
 *
 * Example (Java):
 * ```java
 * public class LoginTest extends KensaPlaywrightUiTest<TheUser> {
 *     @Override
 *     protected TheUser createUser(PlaywrightBrowserDriver driver) {
 *         return new TheUser(driver);
 *     }
 *
 *     @Override
 *     protected void configureLaunchOptions(BrowserType.LaunchOptions options) {
 *         options.setHeadless(false);
 *     }
 * }
 * ```
 */
abstract class KensaPlaywrightUiTest<U : UserStub<PlaywrightBrowserDriver>> : KensaUiTest<PlaywrightBrowserDriver, U>() {

    /** Customise the [BrowserType.LaunchOptions] before the browser is launched. Default: no-op. */
    protected open fun configureLaunchOptions(options: BrowserType.LaunchOptions) {}

    final override fun createDriver(): PlaywrightBrowserDriver {
        val playwright = Playwright.create()
        try {
            val launchOptions = BrowserType.LaunchOptions().setHeadless(true)
            configureLaunchOptions(launchOptions)
            val browser = playwright.chromium().launch(launchOptions)
            return PlaywrightBrowserDriver(browser.newPage(), playwright)
        } catch (t: Throwable) {
            runCatching { playwright.close() }
            throw t
        }
    }
}
