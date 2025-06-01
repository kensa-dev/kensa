package dev.kensa;

import dev.kensa.extension.TestParameterResolver;
import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static dev.kensa.extension.TestParameterResolver.MY_PARAMETER_VALUE;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(TestParameterResolver.class)
public class JavaWithParameterResolverExtensionAndParameterizedTest implements KensaTest, WithHamcrest {

    @Resolve
    private final String aValue = "aStringValue";

    @BeforeEach
    void setUp() {
        Kensa.configure()
                .withValueRenderer(TestParameterResolver.MyArgument.class, MyArgumentRenderer.INSTANCE);
    }

    @ParameterizedTest
    @ValueSource(strings = {"arg1", "arg2"})
    void theTest(@Resolve String parameter1, TestParameterResolver.MyArgument parameter2) {
        assertThat(parameter2.getValue(), is(MY_PARAMETER_VALUE));
        assertThat(Set.of("arg1", "arg2"), hasItem(parameter1));
    }
}
