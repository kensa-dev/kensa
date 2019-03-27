package dev.kensa.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;

import java.util.List;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class TestContainerFactoryTest {

    private TestContainerFactory factory;
    private ExtensionContext extensionContext;

    @BeforeEach
    void setUp() {
        extensionContext = mock(ExtensionContext.class);

        factory = new TestContainerFactory();
    }

    @Test
    void createsTestContainerWithPredictableIterationOrderOfInvocationData() {
        Class<?> testClass = TestClass.class;
        List<String> expected = List.of(
                "test1",
                "test2",
                "test4",
                "test5",
                "parameterizedTest1",
                "parameterizedTest2"
        );

        doReturn(testClass).when(extensionContext).getRequiredTestClass();

        TestContainer result = factory.createFor(extensionContext);
        assertThat(result.invocationData().collect(toList())).extracting("displayName").isEqualTo(expected);
    }

    private static class TestClass {
        @Test
        void test1() {
        }

        @Test
        void test2() {
        }

        @ParameterizedTest
        void parameterizedTest1(String foo) {
        }
        @Test
        void test4() {
        }

        @ParameterizedTest
        void parameterizedTest2(String foo) {
        }

        @Test
        void test5() {
        }
    }
}