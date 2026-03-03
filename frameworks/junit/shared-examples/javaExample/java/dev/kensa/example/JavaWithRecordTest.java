package dev.kensa.example;

import dev.kensa.Action;
import dev.kensa.GivensContext;
import dev.kensa.RenderedValue;
import org.junit.jupiter.api.Test;

public class JavaWithRecordTest extends JavaExampleTest {

    @RenderedValue
    private final MyJavaRecord myJavaRecord = new MyJavaRecord("MyRecordValue");

    @Test
    void passingTest() {
        given(aJavaRecordWithValue(myJavaRecord.value()));
    }

    Action<GivensContext> aJavaRecordWithValue(String value) {
        return (context) -> {
        };
    }

}
