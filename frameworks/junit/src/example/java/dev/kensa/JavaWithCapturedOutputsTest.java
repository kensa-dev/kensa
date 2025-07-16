package dev.kensa;

import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.junit.KensaTest;
import dev.kensa.outputs.CapturedOutput;
import dev.kensa.outputs.CapturedOutputContainer;
import dev.kensa.outputs.CapturedOutputsRegistry;
import org.junit.jupiter.api.Test;

import static dev.kensa.JavaCapturedOutputs.STRING_OUTPUT;
import static dev.kensa.outputs.CapturedOutputsKt.createCapturedOutput;
import static org.hamcrest.CoreMatchers.is;

public class JavaWithCapturedOutputsTest implements KensaTest, WithHamcrest {

    static {
        CapturedOutputsRegistry.registerCapturedOutputs(JavaCapturedOutputs.class, MoreJavaCapturedOutputs.class);
    }

    @Test
    void test1() {
        given(somePrerequisites());

        then(theStringOutput(), is(outputs(STRING_OUTPUT)));
        then(theBooleanFixture(), is(outputs(MoreJavaCapturedOutputs.BOOLEAN_OUTPUT)));
    }

    private Action<GivensContext> somePrerequisites() {
        return (context) -> {
        };
    }

    private StateCollector<String> theStringOutput() {
        return interactions -> outputs(STRING_OUTPUT);
    }

    private StateCollector<Boolean> theBooleanFixture() {
        return interactions -> outputs(MoreJavaCapturedOutputs.BOOLEAN_OUTPUT);
    }
}

class JavaCapturedOutputs implements CapturedOutputContainer {
    public static final CapturedOutput<String> STRING_OUTPUT = createCapturedOutput("JavaStringOutput", String.class);
}

class MoreJavaCapturedOutputs implements CapturedOutputContainer {
    public static final CapturedOutput<Boolean> BOOLEAN_OUTPUT = createCapturedOutput("JavaBooleanOutput", Boolean.class);
}
