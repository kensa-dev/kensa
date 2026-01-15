package dev.kensa.example;

import dev.kensa.Action;
import dev.kensa.ActionContext;
import dev.kensa.GivensContext;
import dev.kensa.StateCollector;
import dev.kensa.hamcrest.WithHamcrest;
import dev.kensa.outputs.CapturedOutput;
import dev.kensa.outputs.CapturedOutputContainer;
import dev.kensa.outputs.CapturedOutputsRegistry;
import org.junit.jupiter.api.Test;

import static dev.kensa.example.JavaCapturedOutputs.STRING_OUTPUT;
import static dev.kensa.example.MoreJavaCapturedOutputs.BOOLEAN_OUTPUT;
import static dev.kensa.outputs.CapturedOutputsKt.createCapturedOutput;
import static org.hamcrest.CoreMatchers.is;

public class JavaWithCapturedOutputsTest extends JavaExampleTest implements WithHamcrest {

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
