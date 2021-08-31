package dev.kensa.state

import dev.kensa.util.Attributes.Companion.emptyAttributes
import dev.kensa.util.Attributes.Companion.of
import dev.kensa.util.KensaMap
import dev.kensa.util.NamedValue
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.stream.Stream

internal class KensaMapTest {
    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> throwsWhenPutWithUniqueKeyDoesNotContainPlaceholder(map: M) {
        assertThatThrownBy { map.putWithUniqueKey("foo", "foo", emptyAttributes()) }.isInstanceOf(IllegalArgumentException::class.java)
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    @Throws(InterruptedException::class)
    internal fun <M : KensaMap<M>> putWithUniqueKeyIsThreadSafe(map: M) {
        val threadCount = 15
        Executors.newFixedThreadPool(threadCount).apply {
            repeat(threadCount) {
                submit { map.putWithUniqueKey("Foo__ idx __", Any(), emptyAttributes()) }
            }
            shutdown()
            awaitTermination(5, TimeUnit.SECONDS)
        }

        assertThat(map.entrySet().size).isEqualTo(threadCount)
        repeat(threadCount) {
            val expectedKey = "Foo" + if (it == 0) "" else " $it"
            assertThat(map.containsKey(expectedKey)).describedAs("Expected key [%s] not found in map", expectedKey).isTrue
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

        assertThat(map.entrySet().size).isEqualTo(threadCount)
        repeat(threadCount) {
            val expectedKey = "String" + if (it == 0) "" else it
            assertThat(map.containsKey(expectedKey)).describedAs("Expected key [%s] not found in map", expectedKey).isTrue
        }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> canPutObject(map: M) {
        val key = "foo"
        val value = "FOO!"
        map.put(key, value)
        assertThat(map.get<String>(key)).isEqualTo(value)
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> canPutObjectWithAttributes(map: M) {
        val key = "foo"
        val value = "FOO!"
        val attributeName = "language"
        val attributeValue = "xml"
        map.put(key, value, of(attributeName, attributeValue))
        assertThat(map.get<String>(key)).isEqualTo(value)

        // Attributes currently only required to be Iterable to allow serialization into Json
        map.entrySet().forEach { entry: KensaMap.Entry ->
            entry.attributes.forEach { (name, value) ->
                assertThat(name).isEqualTo(attributeName)
                assertThat(value).isEqualTo(attributeValue)
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
            assertThat(map.get<Any>(key)).isEqualTo(values[index])
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
            assertThat(map.get<Any>(key)).isEqualTo(values[index])
        }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> iteratesInInsertionOrder(map: M) {
        val size = 10
        val values = (1..size).map { it.toString() }.toList()

        map.putAll(values)

        val result = map.entrySet().map(KensaMap.Entry::value).toList()
        assertThat(result).isEqualTo(values)
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    internal fun <M : KensaMap<M>> canPutCollectionOfNamedValues(map: M) {
        val size = 10
        val values = (0..size).map { NamedValue(it.toString(), "VALUE:$it") }.toList()

        map.putNamedValues(values)

        (0..size).forEach { index ->
            assertThat(map.get<String>(index.toString())).isEqualTo(values[index].value)
        }
    }

    companion object {
        @JvmStatic
        fun mapInstances(): Stream<out KensaMap<*>?> {
            return Stream.of(
                    Givens(),
                    CapturedInteractions()
            )
        }
    }
}