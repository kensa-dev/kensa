package dev.kensa;

import dev.kensa.junit.KensaTest;
import org.junit.jupiter.api.Test;

public class JavaWithRecordTest implements KensaTest {

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

record MyJavaRecord(String value) {}
