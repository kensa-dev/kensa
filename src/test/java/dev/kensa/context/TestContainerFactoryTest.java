package dev.kensa.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;

import java.lang.reflect.Method;
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
    void createsTestContainerWithPredictableIterationOrderOfInvocationData() throws Exception {
        Class<?> testClass = TestClass.class;
        List<Method> expected = List.of(
                TestClass.class.getDeclaredMethod("test1"),
                TestClass.class.getDeclaredMethod("test2"),
                TestClass.class.getDeclaredMethod("test3", String.class),
                TestClass.class.getDeclaredMethod("test4"),
                TestClass.class.getDeclaredMethod("test5", String.class),
                TestClass.class.getDeclaredMethod("test6")
        );

        doReturn(testClass).when(extensionContext).getRequiredTestClass();

        TestContainer result = factory.createFor(extensionContext);
        assertThat(result.invocationData().collect(toList())).extracting("testMethod").isEqualTo(expected);
    }

    private static class TestClass {
        @Test
        void test1() {
        }

        @Test
        void test2() {
        }

        @ParameterizedTest
        void test3(String foo) {
        }

        @Test
        void test4() {
        }

        @ParameterizedTest
        void test5(String foo) {
        }

        @Test
        void test6() {
        }
    }
}