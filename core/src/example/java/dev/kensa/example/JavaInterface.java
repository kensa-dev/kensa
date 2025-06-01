package dev.kensa.example;

import dev.kensa.Highlight;
import dev.kensa.Resolve;

public interface JavaInterface {

    String field4 = "4";

    @Resolve
    String field5 = "5";

    @Highlight
    @Resolve
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
