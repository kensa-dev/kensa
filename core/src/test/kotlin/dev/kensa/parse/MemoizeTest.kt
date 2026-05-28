package dev.kensa.parse

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

internal class MemoizeTest {

    @Test
    fun `caches the provider result so it runs once per key`() {
        val cache = ConcurrentHashMap<String, CompletableFuture<String>>()
        val calls = AtomicInteger(0)
        val provider = { calls.incrementAndGet(); "value" }

        cache.memoize("k", provider) shouldBe "value"
        cache.memoize("k", provider) shouldBe "value"
        cache.memoize("k", provider) shouldBe "value"

        calls.get() shouldBe 1
    }

    @Test
    fun `concurrent callers for the same key invoke the provider exactly once`() {
        val cache = ConcurrentHashMap<String, CompletableFuture<String>>()
        val threads = 16
        val calls = AtomicInteger(0)
        val ready = CountDownLatch(threads)
        val go = CountDownLatch(1)
        val results = ConcurrentLinkedQueue<String>()
        val provider = { calls.incrementAndGet(); "value" }

        val pool = Executors.newFixedThreadPool(threads)
        try {
            repeat(threads) {
                pool.submit {
                    ready.countDown()
                    go.await()
                    results.add(cache.memoize("k", provider))
                }
            }
            ready.await()
            go.countDown()
            pool.shutdown()
            pool.awaitTermination(5, TimeUnit.SECONDS) shouldBe true
        } finally {
            pool.shutdownNow()
        }

        calls.get() shouldBe 1
        results.size shouldBe threads
        results.all { it == "value" } shouldBe true
    }

    @Test
    fun `a slow provider for one key does not block a different key in the same bin`() {
        val cache = ConcurrentHashMap<CollidingKey, CompletableFuture<String>>()
        val providerARunning = CountDownLatch(1)
        val releaseA = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(2)
        try {
            val futureA = pool.submit<String> {
                cache.memoize(CollidingKey(1)) {
                    providerARunning.countDown()
                    releaseA.await()
                    "A"
                }
            }

            providerARunning.await(2, TimeUnit.SECONDS) shouldBe true

            val bResult = pool.submit<String> {
                cache.memoize(CollidingKey(2)) { "B" }
            }.get(2, TimeUnit.SECONDS)
            bResult shouldBe "B"

            releaseA.countDown()
            futureA.get(2, TimeUnit.SECONDS) shouldBe "A"
        } finally {
            pool.shutdownNow()
        }
    }

    @Test
    fun `a failing provider does not poison the cache`() {
        val cache = ConcurrentHashMap<String, CompletableFuture<String>>()
        val calls = AtomicInteger(0)
        val provider: () -> String = {
            val n = calls.incrementAndGet()
            if (n == 1) error("boom") else "value"
        }

        runCatching { cache.memoize("k", provider) }
            .exceptionOrNull()
            ?.message shouldBe "boom"

        cache.memoize("k", provider) shouldBe "value"
        calls.get() shouldBe 2
    }

    private data class CollidingKey(val id: Int) {
        override fun hashCode(): Int = 0
    }
}
