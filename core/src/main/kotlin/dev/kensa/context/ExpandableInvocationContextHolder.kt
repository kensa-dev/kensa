package dev.kensa.context

object ExpandableInvocationContextHolder {
    private val holder = ThreadLocal<ExpandableInvocationContext>()

    fun bindToCurrentThread(testContext: ExpandableInvocationContext) {
        holder.set(testContext)
    }

    fun expandableSentenceInvocationContext(): ExpandableInvocationContext = holder.get()

    fun clearFromThread() {
        holder.remove()
    }
}