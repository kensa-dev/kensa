package dev.kensa.uitesting.junit

import dev.kensa.context.KensaLifecycleManager
import dev.kensa.context.TestContextHolder.testContext
import dev.kensa.uitesting.CapturedScreenshots
import dev.kensa.uitesting.SCREENSHOTS_KEY
import dev.kensa.uitesting.uiTesting
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit 5 extension that manages the [BrowserDriver] and [CapturedScreenshots] lifecycle for
 * [KensaUiTest] subclasses.
 *
 * Registration order matters: this extension must be registered **after** [dev.kensa.junit.KensaExtension]
 * so that JUnit's reversed "after" callback order ensures screenshots are stored into [testContext]
 * **before** KensaExtension.afterTestExecution calls endInvocation (which finalises the invocation).
 *
 * [KensaUiTest] handles registration via its @ExtendWith annotation.
 */
class KensaUiExtension : BeforeEachCallback, AfterTestExecutionCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        CapturedScreenshots.bind(CapturedScreenshots())

        val testInstance = context.requiredTestInstance as? KensaUiTest<*> ?: return
        testInstance.installDriverAndUser()
    }

    override fun afterTestExecution(context: ExtensionContext) {
        val testInstance = context.requiredTestInstance as? KensaUiTest<*>

        if (context.executionException.isPresent && KensaLifecycleManager.current()?.configuration?.uiTesting?.autoScreenshotOnFailure == true) {
            testInstance?._driver?.let { driver ->
                CapturedScreenshots.current().capture(driver, "On failure")
            }
        }

        // Store into attachments before KensaExtension.afterTestExecution finalises the invocation.
        // (afterTestExecution callbacks run in reverse registration order, so this runs first.)
        try {
            testContext().attachments.put(SCREENSHOTS_KEY, CapturedScreenshots.current())
        } catch (e: Exception) {
            System.err.println("KensaUiExtension: failed to attach screenshots to test context: ${e.message}")
            e.printStackTrace(System.err)
        }
    }

    override fun afterEach(context: ExtensionContext) {
        try {
            (context.requiredTestInstance as? KensaUiTest<*>)?.quitDriver()
        } catch (e: Exception) {
            System.err.println("KensaUiExtension: failed to quit browser driver: ${e.message}")
            e.printStackTrace(System.err)
        }
        CapturedScreenshots.clear()
    }
}
