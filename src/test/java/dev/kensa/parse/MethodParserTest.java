package dev.kensa.parse;

import dev.kensa.sentence.Sentences;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ReflectionUtils;

import static org.assertj.core.api.Assertions.assertThat;

class MethodParserTest {

    private MethodParserFactory methodParserFactory;

    @BeforeEach
    void setUp() {
        methodParserFactory = new MethodParserFactory(ReflectionUtils.findMethod(C1.class, "m1").orElseThrow());
    }

    @Test
    void canParseStringsInMethod() {
        var parser = methodParserFactory.createFor(new Object[]{});

        Sentences sentences = parser.sentences();

        assertThat(sentences).isNotNull();
    }

    private static class C1 {
        void m1() {
            var foo = "foo";
        }
    }
}