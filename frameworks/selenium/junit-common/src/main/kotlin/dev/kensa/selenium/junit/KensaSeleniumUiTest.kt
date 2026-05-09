package dev.kensa.selenium.junit

import dev.kensa.selenium.SeleniumBrowserDriver
import dev.kensa.uitesting.UserStub
import dev.kensa.uitesting.junit.KensaUiTest
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions

/**
 * Base class for Kensa UI tests that use Selenium. Handles driver creation —
 * subclasses only need to provide the [UserStub].
 *
 * Defaults: headless Chrome with `--headless=new`, `--no-sandbox`,
 * `--disable-dev-shm-usage` (CI-friendly). Override [configureChromeOptions] to customise.
 *
 * Example (Kotlin):
 * ```kotlin
 * class LoginTest : KensaSeleniumUiTest<TheUser>() {
 *     override fun createUser(driver: SeleniumBrowserDriver) = TheUser(driver)
 *
 *     override fun configureChromeOptions(options: ChromeOptions) {
 *         options.addArguments("--window-size=1920,1080")
 *     }
 * }
 * ```
 *
 * Example (Java):
 * ```java
 * public class LoginTest extends KensaSeleniumUiTest<TheUser> {
 *     @Override
 *     protected TheUser createUser(SeleniumBrowserDriver driver) {
 *         return new TheUser(driver);
 *     }
 *
 *     @Override
 *     protected void configureChromeOptions(ChromeOptions options) {
 *         options.addArguments("--window-size=1920,1080");
 *     }
 * }
 * ```
 */
abstract class KensaSeleniumUiTest<U : UserStub<SeleniumBrowserDriver>> : KensaUiTest<SeleniumBrowserDriver, U>() {

    /** Customise the [ChromeOptions] before the browser is launched. Default: no-op. */
    protected open fun configureChromeOptions(options: ChromeOptions) {}

    final override fun createDriver(): SeleniumBrowserDriver {
        val options = ChromeOptions().apply {
            addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage")
        }
        configureChromeOptions(options)
        return SeleniumBrowserDriver(ChromeDriver(options))
    }
}
