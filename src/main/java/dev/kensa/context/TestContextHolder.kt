package dev.kensa.context

object TestContextHolder {
    private val holder = ThreadLocal<TestContext>()

    @JvmStatic
    fun bindToThread(testContext: TestContext) {
        holder.set(testContext)
    }

    @JvmStatic
    fun testContext(): TestContext = holder.get()

    @JvmStatic
    fun clearFromThread() {
        holder.remove()
    }
}