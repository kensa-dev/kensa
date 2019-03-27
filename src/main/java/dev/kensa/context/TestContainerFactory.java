package dev.kensa.context;

import dev.kensa.state.TestInvocationData;
import dev.kensa.state.TestState;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Collections.synchronizedMap;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

public class TestContainerFactory {

    public TestContainer createFor(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();

        Map<Method, TestInvocationData> invocationData = Stream.concat(
                findAnnotatedMethods(testClass, Test.class, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).stream(),
                findAnnotatedMethods(testClass, ParameterizedTest.class, ReflectionUtils.HierarchyTraversalMode.TOP_DOWN).stream()
        )
                                                               .map(method -> new TestInvocationData(method, deriveDisplayNameFor(method), initialStateFor(method)))
                                                               .collect(
                                                            () -> synchronizedMap(new LinkedHashMap<>()),
                                                            (m, i) -> m.put(i.testMethod(), i),
                                                            Map::putAll
                                                    );

        return new TestContainer(testClass, context.getDisplayName(), invocationData);
    }

    private TestState initialStateFor(Method method) {
        return findAnnotation(method, Disabled.class)
                .map(a -> TestState.Disabled)
                .orElse(TestState.NotExecuted);
    }

    private String deriveDisplayNameFor(Method method) {
        return findAnnotation(method, DisplayName.class)
                .map(DisplayName::value)
                // TODO: build a sentence from the method name
                .orElse(method.getName());
    }
}
