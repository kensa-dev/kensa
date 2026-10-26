package dev.kensa.example;

import dev.kensa.ExpandableRenderedValue;

import java.util.List;

public class JavaWithExpandableValue {

    @ExpandableRenderedValue
    private final List<String> theObservedFields = List.of("John", "Smith");

    void expandableValueField() {
        assertThatHas(theObservedFields);
    }

    void qualifiedCallToExpandableValueMethod() {
        assertThatHas(this.theObservedDetails());
    }

    @ExpandableRenderedValue
    private List<String> theObservedDetails() {
        return theObservedFields;
    }

    private void assertThatHas(List<String> values) {
    }
}
