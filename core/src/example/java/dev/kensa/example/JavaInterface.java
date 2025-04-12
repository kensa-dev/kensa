package dev.kensa.example;

import dev.kensa.Highlight;
import dev.kensa.Scenario;
import dev.kensa.SentenceValue;

public interface JavaInterface {

    String field4 = "4";

    @Scenario
    String field5 = "5";

    @Highlight
    @SentenceValue
    String field6 = "6";

    default void doSomething() {}

    default void methodOnInterface() {
        doSomething();
    }

//    @NestedSentence
//    default void nestedSentence() {
//        assertThat("abc").contains("a");
//    }
}
