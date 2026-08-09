package dev.kensa.context

import dev.kensa.KensaException

object TestContextHolder {
    private val holder = ThreadLocal<TestContext>()

    @JvmStatic
    fun bindToCurrentThread(testContext: TestContext) {
        holder.set(testContext)
    }

    @JvmStatic
    fun testContext(): TestContext = testContextOrNull() ?: throw KensaException("No Kensa test context is bound to the current thread")

    @JvmStatic
    fun testContextOrNull(): TestContext? = holder.get()

    @JvmStatic
    fun clearFromThread() {
        holder.remove()
    }
}