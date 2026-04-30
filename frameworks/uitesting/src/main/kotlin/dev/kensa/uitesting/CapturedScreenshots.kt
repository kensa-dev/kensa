package dev.kensa.uitesting

import dev.kensa.attachments.TypedKey

/** A single screenshot taken during a test invocation. */
class CapturedScreenshot(val label: String, val format: String, val bytes: ByteArray)

/** Thread-local registry of screenshots captured during a test invocation. */
class CapturedScreenshots {
    private val list = mutableListOf<CapturedScreenshot>()

    fun capture(driver: BrowserDriver, label: String) {
        list += CapturedScreenshot(label = label, format = "png", bytes = driver.takeScreenshot())
    }

    fun all(): List<CapturedScreenshot> = list.toList()

    fun clear() = list.clear()

    companion object {
        private val holder = ThreadLocal<CapturedScreenshots>()

        fun bind(screenshots: CapturedScreenshots) {
            holder.set(screenshots)
        }

        fun current(): CapturedScreenshots = holder.get()
            ?: error("No CapturedScreenshots bound on this thread. Ensure the KensaUiExtension is installed (extend KensaUiTest, or @ExtendWith(KensaUiExtension::class)).")

        fun clear() {
            holder.remove()
        }
    }
}

@JvmField
val SCREENSHOTS_KEY: TypedKey<CapturedScreenshots> = TypedKey("screenshots")
