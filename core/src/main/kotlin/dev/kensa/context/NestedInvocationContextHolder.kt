package dev.kensa.context

object NestedInvocationContextHolder {
    private val holder = ThreadLocal<NestedInvocationContext>()

    fun bindToCurrentThread(testContext: NestedInvocationContext) {
        holder.set(testContext)
    }

    fun nestedSentenceInvocationContext(): NestedInvocationContext = holder.get()

    fun clearFromThread() {
        holder.remove()
    }
}