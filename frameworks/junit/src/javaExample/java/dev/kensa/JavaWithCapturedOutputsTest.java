package dev.kensa;

import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.junit.KensaTest;
import dev.kensa.outputs.CapturedOutput;
import dev.kensa.outputs.CapturedOutputContainer;
import dev.kensa.outputs.CapturedOutputsRegistry;
import org.junit.jupiter.api.Test;

import static dev.kensa.JavaCapturedOutputs.STRING_OUTPUT;
import static dev.kensa.MoreJavaCapturedOutputs.BOOLEAN_OUTPUT;
import static dev.kensa.outputs.CapturedOutputsKt.createCapturedOutput;
import static org.hamcrest.CoreMatchers.is;

public class JavaWithCapturedOutputsTest implements KensaTest, WithHamcrest {

    static {
        CapturedOutputsRegistry.registerCapturedOutputs(JavaCapturedOutputs.class, MoreJavaCapturedOutputs.class);
    }

    private String theStringOutput = "theOutput";
    private Boolean theBooleanOutput = true;

    @Test
    void test1() {
        given(somePrerequisites());

        whenever(theOutputsAreGenerated());

        then(theStringOutput(), is(outputs(STRING_OUTPUT)));
        then(theBooleanFixture(), is(outputs(BOOLEAN_OUTPUT)));
    }

    private Action<GivensContext> somePrerequisites() {
        return (context) -> {
        };
    }

    private Action<ActionContext> theOutputsAreGenerated() {
        return (context) -> {
            context.getOutputs().put(STRING_OUTPUT, theStringOutput);
            context.getOutputs().put(BOOLEAN_OUTPUT, theBooleanOutput);
        };
    }

    private StateCollector<String> theStringOutput() {
        return context -> context.getOutputs().get(STRING_OUTPUT);
    }

    private StateCollector<Boolean> theBooleanFixture() {
        return context -> context.getOutputs().get(BOOLEAN_OUTPUT);
    }
}

class JavaCapturedOutputs implements CapturedOutputContainer {
    public static final CapturedOutput<String> STRING_OUTPUT = createCapturedOutput("JavaStringOutput", String.class);
}

class MoreJavaCapturedOutputs implements CapturedOutputContainer {
    public static final CapturedOutput<Boolean> BOOLEAN_OUTPUT = createCapturedOutput("JavaBooleanOutput", Boolean.class);
}
