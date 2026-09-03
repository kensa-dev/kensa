package dev.kensa.context

import dev.kensa.Kensa
import dev.kensa.KensaInternalApi
import kotlinx.coroutines.ThreadContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Carries Kensa's thread-bound contexts across a coroutine dispatch.
 *
 * Kensa binds the test context, and the two compiler-plugin invocation contexts, to the thread running
 * the test. Multi-assertion `thenEventually { }` and `thenContinually { }` blocks run each check on a
 * `Dispatchers.IO` thread, and coroutines do not carry thread locals across a dispatch, so without this
 * a check finds no bound context: a `by fixtures(...)` delegate fails, `WithFixturesAndOutputs` fails,
 * and a `@RenderedValue` or `@ExpandableSentence` helper called from the check has nowhere to record.
 *
 * Capture the calling thread's contexts with [kensaThreadContext] and add the result to the coroutine
 * context. Each dispatch then binds them on the running thread and restores whatever was there before.
 */
@KensaInternalApi
class KensaThreadContextElement internal constructor(
    private val testContext: TestContext?,
    private val expandableInvocationContext: ExpandableInvocationContext?,
    private val renderedValueInvocationContext: RenderedValueInvocationContext?,
) : ThreadContextElement<KensaThreadContextElement.State> {

    class State internal constructor(
        internal val testContext: TestContext?,
        internal val expandableInvocationContext: ExpandableInvocationContext?,
        internal val renderedValueInvocationContext: RenderedValueInvocationContext?,
    )

    override val key: CoroutineContext.Key<KensaThreadContextElement> get() = Key

    override fun updateThreadContext(context: CoroutineContext): State {
        val previous = State(
            TestContextHolder.testContextOrNull(),
            ExpandableInvocationContextHolder.boundContextOrNull(),
            RenderedValueInvocationContextHolder.boundContextOrNull(),
        )

        bind(testContext, expandableInvocationContext, renderedValueInvocationContext)

        return previous
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: State) {
        bind(oldState.testContext, oldState.expandableInvocationContext, oldState.renderedValueInvocationContext)
    }

    private fun bind(
        testContext: TestContext?,
        expandableInvocationContext: ExpandableInvocationContext?,
        renderedValueInvocationContext: RenderedValueInvocationContext?,
    ) {
        testContext?.let(TestContextHolder::bindToCurrentThread) ?: TestContextHolder.clearFromThread()
        expandableInvocationContext?.let(ExpandableInvocationContextHolder::bindToCurrentThread)
            ?: ExpandableInvocationContextHolder.clearFromThread()
        renderedValueInvocationContext?.let(RenderedValueInvocationContextHolder::bindToCurrentThread)
            ?: RenderedValueInvocationContextHolder.clearFromThread()
    }

    companion object Key : CoroutineContext.Key<KensaThreadContextElement>
}

/**
 * Captures the Kensa contexts bound to the calling thread, for propagation into coroutines that may run
 * on another thread. See [KensaThreadContextElement].
 *
 * User-registered coroutine context providers (`withCoroutineContextProviders`) are invoked here, on the
 * calling thread, and their elements folded into the returned context — so a
 * `ThreadLocal.asContextElement()` provider captures the value bound to the test thread at this moment.
 */
@KensaInternalApi
fun kensaThreadContext(): CoroutineContext = Kensa.configuration.coroutineContextProviders.fold(
    KensaThreadContextElement(
        TestContextHolder.testContextOrNull(),
        ExpandableInvocationContextHolder.boundContextOrNull(),
        RenderedValueInvocationContextHolder.boundContextOrNull(),
    ) as CoroutineContext
) { context, provider -> context + provider() }
