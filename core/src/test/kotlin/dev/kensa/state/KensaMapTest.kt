package dev.kensa.state

import dev.kensa.util.Attributes.Companion.of
import dev.kensa.util.KensaMap
import dev.kensa.util.NamedValue
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

internal class KensaMapTest {
    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> throwsWhenPutWithUniqueKeyDoesNotContainPlaceholder(map: M) {
        shouldThrowExactly<IllegalArgumentException> { map.putWithUniqueKey("foo", "foo") }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    @Throws(InterruptedException::class)
    internal fun <M : KensaMap<M>> putWithUniqueKeyIsThreadSafe(map: M) {
        val threadCount = 15
        Executors.newFixedThreadPool(threadCount).apply {
            repeat(threadCount) {
                submit { map.putWithUniqueKey("Foo__ idx __", Any()) }
            }
            shutdown()
            awaitTermination(5, TimeUnit.SECONDS)
        }

        map.entrySet().size shouldBe threadCount
        repeat(threadCount) {
            val expectedKey = "Foo" + if (it == 0) "" else " $it"
            map.containsKey(expectedKey).shouldBe(true)
        }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    @Throws(InterruptedException::class)
    internal fun <M : KensaMap<M>> putWithValueOnlyIsThreadSafe(map: M) {
        val threadCount = 15
        Executors.newFixedThreadPool(threadCount).apply {
            repeat(threadCount) { value: Int ->
                submit { map.put("foo$value") }
            }
            shutdown()
            awaitTermination(5, TimeUnit.SECONDS)
        }

        map.entrySet().size shouldBe threadCount
        repeat(threadCount) {
            val expectedKey = "String" + if (it == 0) "" else it
            map.containsKey(expectedKey).shouldBeTrue()
        }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> canPutObject(map: M) {
        val key = "foo"
        val value = "FOO!"
        map.put(key, value)
        map.get<String>(key) shouldBe value
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> canPutObjectWithAttributes(map: M) {
        val key = "foo"
        val value = "FOO!"
        val attributeName = "language"
        val attributeValue = "xml"
        map.put(key, value, attributes = of(attributeName, attributeValue))
        map.get<String>(key) shouldBe value

        // Attributes currently only required to be Iterable to allow serialization into Json
        map.entrySet().forEach { entry: KensaMap.Entry ->
            entry.attributes.forEach { (name, value) ->
                name shouldBe attributeName
                value shouldBe attributeValue
            }
        }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> canPutMultipleObjectsWithDefaultKey_SinglePut(map: M) {
        val size = 5
        val values = (1..size).map { Any() }.toList()

        values.forEach { value: Any -> map.put(value) }

        repeat(size) { index ->
            val key = if (index == 0) "Object" else "Object$index"
            map.get<Any>(key) shouldBe values[index]
        }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> canPutMultipleObjectsWithDefaultKey_MultiPut(map: M) {
        val size = 5
        val values = (1..size).map { Any() }.toList()

        map.putAll(values)

        repeat(size) { index ->
            val key = if (index == 0) "Object" else "Object$index"
            map.get<Any>(key) shouldBe values[index]
        }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> iteratesInInsertionOrder(map: M) {
        val size = 10
        val values = (1..size).map { it.toString() }.toList()

        map.putAll(values)

        val result = map.entrySet().map(KensaMap.Entry::value).toList()
        result shouldBe values
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> iteratesInTimestampOrder(map: M) {
        val now = System.currentTimeMillis() - 1

        map.putAll(listOf(2, 3, 4, 5))
        map.put(1, now)

        val result = map.entrySet().map(KensaMap.Entry::value).toList()
        result shouldBe listOf(1, 2, 3, 4, 5)
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> canPutCollectionOfNamedValues(map: M) {
        val size = 10
        val values = (0..size).map { NamedValue(it.toString(), "VALUE:$it") }.toList()

        map.putNamedValues(values)

        (0..size).forEach { index ->
            map.get<String>(index.toString()) shouldBe values[index].value
        }
    }

    companion object {
        @JvmStatic
        fun mapInstances(): Stream<out KensaMap<*>?> {
            return Stream.of(
                Givens(),
                CapturedInteractions(SetupStrategy.Ignored)
            )
        }
    }
}