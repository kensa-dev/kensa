package dev.kensa.example;

import dev.kensa.Highlight;
import dev.kensa.RenderedValue;

public interface JavaInterface {

    String field4 = "4";

    @RenderedValue
    String field5 = "5";

    @Highlight
    @RenderedValue
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
