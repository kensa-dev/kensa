package dev.kensa.context

object RenderedValueInvocationContextHolder {
    private val holder = ThreadLocal<RenderedValueInvocationContext>()

    fun bindToCurrentThread(testContext: RenderedValueInvocationContext) {
        holder.set(testContext)
    }

    fun renderedValueInvocationContext(): RenderedValueInvocationContext = holder.get()

    fun clearFromThread() {
        holder.remove()
    }
}