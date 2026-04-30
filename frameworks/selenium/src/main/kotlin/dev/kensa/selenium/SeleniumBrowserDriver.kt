package dev.kensa.selenium

import dev.kensa.uitesting.BrowserDriver
import org.openqa.selenium.OutputType
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.WebDriver
import org.openqa.selenium.chromium.ChromiumDriver
import java.util.Base64

/**
 * [BrowserDriver] implementation backed by a Selenium [WebDriver].
 *
 * Full-page screenshots: Chromium-based drivers (Chrome, Edge) use the CDP
 * `Page.captureScreenshot { captureBeyondViewport: true }` command. Other drivers
 * fall back to viewport-only screenshots via [TakesScreenshot].
 *
 * Example:
 * ```kotlin
 * override fun createDriver() = SeleniumBrowserDriver(ChromeDriver())
 * ```
 */
class SeleniumBrowserDriver(val webDriver: WebDriver) : BrowserDriver {

    override fun takeScreenshot(): ByteArray {
        if (webDriver is ChromiumDriver) {
            val result = webDriver.executeCdpCommand(
                "Page.captureScreenshot",
                mapOf(
                    "format" to "png",
                    "captureBeyondViewport" to true,
                )
            )
            val data = result["data"] as String
            return Base64.getDecoder().decode(data)
        }
        require(webDriver is TakesScreenshot) {
            "WebDriver implementation ${webDriver::class.simpleName} does not implement TakesScreenshot"
        }
        return webDriver.getScreenshotAs(OutputType.BYTES)
    }

    override fun quit() = webDriver.quit()
}
