package dev.kensa.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ReflectionUtil {

    public static <A extends Annotation> Optional<A> getAnnotation(AnnotatedElement element, Class<A> annotation) {
        return Optional.ofNullable(element.getAnnotation(annotation));
    }

    public static Stream<Method> testMethodsOf(Class<?> target) {
        return methodsAnnotatedWithAny(target, Test.class, ParameterizedTest.class);
    }

    @SafeVarargs
    public static Stream<Method> methodsAnnotatedWithAny(Class<?> target, Class<? extends Annotation>... annotations) {
        return findAnnotatedMethods(target, new LinkedHashSet<>(), annotations).stream();
    }

    @SafeVarargs
    private static Set<Method> findAnnotatedMethods(Class<?> target, Set<Method> methods, Class<? extends Annotation>... annotations) {
        Arrays.stream(target.getInterfaces())
              .forEach(i -> findAnnotatedMethods(i, methods, annotations));

        Class<?> c = target.getSuperclass();
        if (c != null && c != Object.class) {
            findAnnotatedMethods(c, methods, annotations);
        }

        Arrays.stream(target.getDeclaredMethods())
              .filter(isAnnotatedWith(annotations))
              .forEach(methods::add);

        return methods;
    }

    @SafeVarargs
    private static Predicate<Method> isAnnotatedWith(Class<? extends Annotation>... annotations) {
        return m -> Arrays.stream(annotations).anyMatch(m::isAnnotationPresent);
    }

    private ReflectionUtil() {
    }
}
