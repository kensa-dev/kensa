package dev.kensa.context

import dev.kensa.Kensa
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asContextElement
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Test
import kotlin.coroutines.CoroutineContext

class CoroutineContextProvidersTest {

    private val userThreadLocal = ThreadLocal<String>()

    @Test
    fun `a configured provider propagates a user thread local across a dispatch`() {
        val provider: () -> CoroutineContext = { userThreadLocal.asContextElement() }
        Kensa.configure().withCoroutineContextProviders(provider)
        try {
            userThreadLocal.set("tracking-id-123")

            val seen = runBlocking(kensaThreadContext()) {
                withContext(Dispatchers.IO) { userThreadLocal.get() }
            }

            seen shouldBe "tracking-id-123"
        } finally {
            Kensa.konfigure { coroutineContextProviders.remove(provider) }
            userThreadLocal.remove()
        }
    }

    @Test
    fun `a provider captures the calling thread's value each time the context is assembled`() {
        val provider: () -> CoroutineContext = { userThreadLocal.asContextElement() }
        Kensa.configure().withCoroutineContextProviders(provider)
        try {
            userThreadLocal.set("first")
            val first = runBlocking(kensaThreadContext()) {
                withContext(Dispatchers.IO) { userThreadLocal.get() }
            }

            userThreadLocal.set("second")
            val second = runBlocking(kensaThreadContext()) {
                withContext(Dispatchers.IO) { userThreadLocal.get() }
            }

            first shouldBe "first"
            second shouldBe "second"
        } finally {
            Kensa.konfigure { coroutineContextProviders.remove(provider) }
            userThreadLocal.remove()
        }
    }
}
