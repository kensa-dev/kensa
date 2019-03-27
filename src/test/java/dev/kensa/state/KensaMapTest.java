package dev.kensa.state;

import dev.kensa.util.Attributes;
import dev.kensa.util.KensaMap;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class KensaMapTest {

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
            var key = index == 0 ? "Object" : "Object " + index;
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
            var key = index == 0 ? "Object" : "Object " + index;
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

    private static Stream<? extends KensaMap> mapInstances() {
        return Stream.of(
                new Givens(),
                new CapturedInteractions()
        );
    }
}