package dev.kensa.uitesting

class FakeBrowserDriver(
    private val pngBytes: ByteArray = byteArrayOf(0x89.toByte(), 'P'.code.toByte(), 'N'.code.toByte(), 'G'.code.toByte())
) : BrowserDriver {
    var quitCalled: Boolean = false
        private set

    override fun takeScreenshot(): ByteArray = pngBytes
    override fun quit() { quitCalled = true }
}
