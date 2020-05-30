package dev.kensa.java;

import dev.kensa.ActionUnderTest;
import dev.kensa.GivensBuilder;
import dev.kensa.GivensWithInteractionsBuilder;
import dev.kensa.KensaExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.kensa.context.TestContextHolder.testContext;

@ExtendWith(KensaExtension.class)
public interface JavaKensaTest {

    default void disableInteractionTestGroup() {
        testContext().disableInteractionTestGroup();
    }

    default void given(GivensBuilder builder) {
        testContext().given(builder);
    }

    default void and(GivensBuilder builder) {
        given(builder);
    }

    default void given(GivensWithInteractionsBuilder builder) {
        testContext().given(builder);
    }

    default void and(GivensWithInteractionsBuilder builder) {
        given(builder);
    }

    default void when(ActionUnderTest action) {
        testContext().whenever(action);
    }
}
