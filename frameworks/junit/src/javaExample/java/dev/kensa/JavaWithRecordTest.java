package dev.kensa;

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
