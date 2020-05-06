package dev.kensa.state;

import dev.kensa.util.Attributes;
import dev.kensa.util.KensaMap;
import dev.kensa.util.NamedValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.kensa.util.Attributes.emptyAttributes;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KensaMapTest {

    @ParameterizedTest
    @MethodSource("mapInstances")
    <M extends KensaMap<M>> void throwsWhenPutWithUniqueKeyDoesNotContainPlaceholder(M map) {
        assertThatThrownBy(() -> map.putWithUniqueKey("foo", "foo", emptyAttributes())).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    <M extends KensaMap<M>> void putWithUniqueKeyIsThreadSafe(M map) throws InterruptedException {
        var threadCount = 15;
        var executorService = Executors.newFixedThreadPool(threadCount);

        IntStream.range(0, threadCount)
                 .forEach(value -> executorService.submit(() -> map.putWithUniqueKey("Foo__ idx __", new Object(), emptyAttributes())));

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(map.entrySet().size()).isEqualTo(threadCount);
        IntStream.range(0, threadCount)
                 .forEach(value -> {
                     var expectedKey = "Foo" + (value == 0 ? "" : " " + value);

                     assertThat(map.containsKey(expectedKey)).describedAs("Expected key [%s] not found in map", expectedKey).isTrue();
                 });
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    <M extends KensaMap<M>> void putWithValueOnlyIsThreadSafe(M map) throws InterruptedException {
        var threadCount = 15;
        var executorService = Executors.newFixedThreadPool(threadCount);

        IntStream.range(0, threadCount)
                 .forEach(value -> executorService.submit(() -> map.put("foo" + value)));

        executorService.shutdown();
        executorService.awaitTermination(5, TimeUnit.SECONDS);

        assertThat(map.entrySet().size()).isEqualTo(threadCount);
        IntStream.range(0, threadCount)
                 .forEach(value -> {
                     var expectedKey = "String" + (value == 0 ? "" : value);

                     assertThat(map.containsKey(expectedKey)).describedAs("Expected key [%s] not found in map", expectedKey).isTrue();
                 });
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    <M extends KensaMap<M>> void canPutObject(M map) {
        var key = "foo";
        var value = "FOO!";

        map.put(key, value);

        assertThat(map.get(key, String.class)).isEqualTo(value);
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    <M extends KensaMap<M>> void canPutObjectWithAttributes(M map) {
        var key = "foo";
        var value = "FOO!";
        var attributeName = "language";
        var attributeValue = "xml";

        map.put(key, value, Attributes.of(attributeName, attributeValue));

        assertThat(map.get(key, String.class)).isEqualTo(value);
        // Attributes currently only required to be Iterable to allow serialization into Json
        map.entrySet().forEach(entry -> entry.attributes().forEach(attribute -> {
            assertThat(attribute.name()).isEqualTo(attributeName);
            assertThat(attribute.value()).isEqualTo(attributeValue);
        }));
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    <M extends KensaMap<M>> void canPutMultipleObjectsWithDefaultKey_SinglePut(M map) {
        var values = IntStream.range(0, 5)
                              .mapToObj(value -> new Object())
                              .collect(toList());

        values.forEach(map::put);

        for (var index = 0; index < values.size(); index++) {
            var key = index == 0 ? "Object" : "Object" + index;
            assertThat(map.get(key, Object.class)).isEqualTo(values.get(index));
        }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    <M extends KensaMap<M>> void canPutMultipleObjectsWithDefaultKey_MultiPut(M map) {
        var values = IntStream.range(0, 5)
                              .mapToObj(value -> new Object())
                              .collect(toList());

        map.putAll(values);

        for (var index = 0; index < values.size(); index++) {
            var key = index == 0 ? "Object" : "Object" + index;
            assertThat(map.get(key, Object.class)).isEqualTo(values.get(index));
        }
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    <M extends KensaMap<M>> void iteratesInInsertionOrder(M map) {
        var values = IntStream.range(0, 10)
                              .mapToObj(String::valueOf)
                              .collect(toList());

        map.putAll(values);

        var result = map.entrySet().stream().map(KensaMap.Entry::value).collect(toList());

        assertThat(result).isEqualTo(values);
    }

    @ParameterizedTest
    @MethodSource("mapInstances")
    <M extends KensaMap<M>> void canPutCollectionOfNamedValues(M map) {
        var values = IntStream.range(0, 10)
                              .mapToObj(value -> new NamedValue(String.valueOf(value), "VALUE:" + value))
                              .collect(toList());

        map.putNamedValues(values);

        for (var index = 0; index < values.size(); index++) {
            assertThat(map.get(String.valueOf(index), String.class)).isEqualTo(values.get(index).value());
        }
    }

    private static Stream<? extends KensaMap<?>> mapInstances() {
        return Stream.of(
                new Givens(),
                new CapturedInteractions()
        );
    }
}