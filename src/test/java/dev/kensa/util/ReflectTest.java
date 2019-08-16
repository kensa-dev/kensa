package dev.kensa.util;

import dev.kensa.Notes;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static dev.kensa.util.Reflect.getAnnotation;
import static dev.kensa.util.Reflect.testMethodsOf;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

class ReflectTest {

    @Test
    void canGetAPrivateFieldValue() {
        String privateValue = "A Value";
        SomeClass target = new SomeClass(privateValue);

        assertThat(Reflect.fieldValue(target, "someField", String.class)).isEqualTo(privateValue);
    }

    @Test
    void canGetValueOfFieldViaSupplierWhenFieldTypeIsSupplier() {
        String privateValue = "A Value";
        SomeClass target = new SomeClass(privateValue);

        assertThat(Reflect.fieldValue(target, "valueSupplier", String.class)).isEqualTo(privateValue);
    }

    @Test
    void canInvokeAMethodAndGetReturnedValue() {
        String privateValue = "A Value";
        SomeClass target = new SomeClass(privateValue);

        assertThat(Reflect.invokeMethod(target, "getSomeField", String.class)).isEqualTo(privateValue);
    }

    @Test
    void canInvokeADefaultMethodOnAnInterface() {
        SomeClass target = new SomeClass("A Value");

        assertThat(Reflect.invokeMethod(target, "defaultMethod", String.class)).isEqualTo("defaultResult");
    }

    @Test
    void canInvokeAnOverriddenDefaultMethod() {
        SomeClass target = new SomeSubClass();

        assertThat(Reflect.invokeMethod(target, "defaultMethod", String.class)).isEqualTo("overriddenResult");
    }

    @Test
    void canFindAndInvokeInInterfaceHierarchy() {
        SomeOtherSubClass target = new SomeOtherSubClass();

        assertThat(Reflect.invokeMethod(target, "defaultMethod", String.class)).isEqualTo("defaultResult");
    }

    @Test
    void canGetAnAnnotationFromAClassElement() {
        Optional<Notes> result = getAnnotation(Superclass.class, Notes.class);

        assertThat(result.map(Notes::value)).hasValue("Some notes");
    }

    @Test
    void canGetAnAnnotationFromAMethodElement() throws Exception {
        Optional<Notes> result = getAnnotation(Superclass.class.getDeclaredMethod("test1"), Notes.class);

        assertThat(result.map(Notes::value)).hasValue("Some notes");
    }

    @Test
    void returnsEmptyStreamWhenNoTestMethodsFound() {
        assertThat(testMethodsOf(NotAnnotated.class)).isEmpty();
    }

    @Test
    void canFindTestMethodsInSingleClass() throws Exception {
        List<Method> expected = List.of(
                Superclass.class.getDeclaredMethod("test1"),
                Superclass.class.getDeclaredMethod("test2")
        );

        List<Method> result = testMethodsOf(Superclass.class).collect(toList());

        assertThat(result).containsAll(expected);
    }

    @Test
    void canFindTestMethodsInClassHierarchy() throws Exception {
        List<Method> expected = List.of(
                Superclass.class.getDeclaredMethod("test1"),
                Superclass.class.getDeclaredMethod("test2"),
                Subclass.class.getDeclaredMethod("test3"),
                Subclass.class.getDeclaredMethod("test4"),
                Subclass.class.getDeclaredMethod("test5", String.class)
        );

        List<Method> result = testMethodsOf(Subclass.class).collect(toList());

        assertThat(result).containsAll(expected);
    }

    @Test
    void canFindTestMethodsInheritedFromInterfaces() throws Exception {
        List<Method> expected = List.of(
                TestInterface.class.getDeclaredMethod("test6"),
                TestInterface.class.getDeclaredMethod("test7", String.class)
        );

        List<Method> result = testMethodsOf(TestImpl.class).collect(toList());

        assertThat(result).containsAll(expected);
    }

    static class NotAnnotated {
        void foo() {}
    }

    interface SomeInterface {
        default String defaultMethod() {
            return "defaultResult";
        }
    }

    interface SomeSubInterface extends SomeInterface {
    }

    static class SomeClass implements SomeInterface {
        private final String someField;

        private final Supplier<String> valueSupplier = this::getSomeField;

        SomeClass(String someField) {this.someField = someField;}
        private String getSomeField() {
            return someField;
        }

    }

    static class SomeSubClass extends SomeClass {
        public SomeSubClass() {
            super("foo");
        }

        @Override
        public String defaultMethod() {
            return "overriddenResult";
        }
    }

    static class SomeOtherSubClass implements SomeSubInterface {
    }

    @Disabled
    @Notes("Some notes")
    static class Superclass {

        @Test
        @Notes("Some notes")
        void test1() {}

        @Test
        void test2() {}

        void notATestMethod() {}
    }

    @Disabled
    static class Subclass extends Superclass {

        @Test
        void test3() {}

        @Test
        void test4() {}

        @ParameterizedTest
        void test5(String foo) {}
    }

    interface TestInterface {

        @Test
        void test6();

        @ParameterizedTest
        void test7(String foo);
    }

    static class TestImpl implements TestInterface {
        @Override
        public void test6() {}

        @Override
        public void test7(String foo) {}
    }
}