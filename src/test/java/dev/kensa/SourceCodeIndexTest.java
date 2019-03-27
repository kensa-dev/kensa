package dev.kensa;

import dev.kensa.util.SourceCodeIndex;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class SourceCodeIndexTest {


    @Test
    void canLocateByInnerClass() {
        Path path = SourceCodeIndex.locate(InnerClass.class);

        assertThat(path).isNotNull();
    }


    static class InnerClass {

    }
}