// Snippet source for kensa.dev/docs/writing-fluent-tests.md + api/annotations.md — Java Tabular example
package apidocs;

import kotlin.Pair;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TabularSnippets {

    static class ShipmentRecord {
        String field(String name) { return "value"; }
    }

    private final ShipmentRecord dispatched = new ShipmentRecord();

    private List<Pair<String, String>> theShipmentFields() {
        List<Pair<String, String>> fields = List.of(
                new Pair<>("PostCode", "SW1A 1AA"),
                new Pair<>("CountryCode", "GB"),
                new Pair<>("ServiceLevel", "Express")
        );
        fields.forEach(f -> assertThat(dispatched.field(f.getFirst())).isEqualTo(f.getSecond()));
        return fields;
    }
}
