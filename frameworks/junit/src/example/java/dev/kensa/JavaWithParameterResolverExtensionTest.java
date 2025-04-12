package dev.kensa;

import dev.kensa.extension.TestParameterResolver;
import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.kensa.extension.TestParameterResolver.MY_PARAMETER_VALUE;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(TestParameterResolver.class)
public class JavaWithParameterResolverExtensionTest implements KensaTest, WithHamcrest {

    @SentenceValue
    private final String aValue = "aStringValue";

    @Test
    void theTest(TestParameterResolver.MyArgument parameter) {
        assertThat(parameter.getValue(), is(MY_PARAMETER_VALUE));
    }
}
