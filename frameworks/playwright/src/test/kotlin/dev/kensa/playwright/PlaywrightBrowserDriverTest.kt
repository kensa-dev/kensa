package dev.kensa.playwright

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class PlaywrightBrowserDriverTest {

    @Test
    fun `quit closes browser and playwright then propagates the original exception when context_close throws`() {
        val playwright = mock<Playwright>()
        val browser = mock<Browser>()
        val context = mock<BrowserContext> {
            on { browser() } doReturn browser
        }
        val page = mock<Page> {
            on { context() } doReturn context
        }
        whenever(context.close()).doThrow(RuntimeException("context boom"))

        val ex = shouldThrow<RuntimeException> { PlaywrightBrowserDriver(page, playwright).quit() }

        ex.message shouldBe "context boom"
        verify(browser).close()
        verify(playwright).close()
    }

    @Test
    fun `quit closes playwright then propagates the original exception when browser_close throws`() {
        val playwright = mock<Playwright>()
        val browser = mock<Browser>()
        val context = mock<BrowserContext> {
            on { browser() } doReturn browser
        }
        val page = mock<Page> {
            on { context() } doReturn context
        }
        whenever(browser.close()).doThrow(RuntimeException("browser boom"))

        val ex = shouldThrow<RuntimeException> { PlaywrightBrowserDriver(page, playwright).quit() }

        ex.message shouldBe "browser boom"
        verify(playwright).close()
    }
}
