package dev.kensa.util;

import dev.kensa.KensaException;
import dev.kensa.function.Unchecked;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ReflectionUtil {

    public static <A extends Annotation> Optional<A> getAnnotation(AnnotatedElement element, Class<A> annotation) {
        return Optional.ofNullable(element.getAnnotation(annotation));
    }

    public static Stream<Method> testMethodsOf(Class<?> target) {
        return methodsAnnotatedWithAny(target, Test.class, ParameterizedTest.class);
    }

    public static <T> T fieldValue(Object target, String name, Class<T> type) {
        return findField(target.getClass(), name)
                .map(Unchecked.function(field -> {
                    field.setAccessible(true);
                    return field.get(target);
                }))
                .map(type::cast)
                .orElse(null);
    }

    public static <T> T invokeMethod(Object target, String name, Class<T> requiredType) {
        try {
            return requiredType.cast(findMethod(target.getClass(), name).invoke(target));
        } catch (Exception e) {
            throw new KensaException(String.format("Unable to invoke method [%s] on class [%s]", name, name), e);
        }
    }

    public static Map<String, NameValuePair> fieldValuesOf(Object target) {
        try {
            Map<String, NameValuePair> values = new HashMap<>();
            Class<?> aClass = target.getClass();

            while (aClass != Object.class) {
                Field[] declaredFields = aClass.getDeclaredFields();
                for (Field field : declaredFields) {
                    field.setAccessible(true);
                    NameValuePair nameValuePair = new NameValuePair(field.getName(), field.get(target));
                    values.put(nameValuePair.name(), nameValuePair);
                }
                aClass = aClass.getSuperclass();
            }

            return values;
        } catch (IllegalAccessException e) {
            throw new KensaException(String.format("Unable to get field values of class [%s]", target.getClass().getName()), e);
        }
    }

    private static Method findMethod(Class<?> target, String name) {
        try {
            Method method = target.getDeclaredMethod(name);
            method.setAccessible(true);
            return method;
        } catch (NoSuchMethodException e) {
            Class<?> superclass = target.getSuperclass();
            if (superclass != Object.class) {
                return findMethod(superclass, name);
            }

            throw new KensaException(String.format("Unable to find method [%s] on class [%s]", name, target.getName()), e);
        }
    }

    private static Optional<Field> findField(Class<?> target, String name) {
        if (target == null || target == Object.class) {
            return Optional.empty();
        }

        for (Field field : target.getDeclaredFields()) {
            if (field.getName().equals(name)) {
                return Optional.of(field);
            }
        }

        return findField(target.getSuperclass(), name);
    }

    @SafeVarargs
    private static Stream<Method> methodsAnnotatedWithAny(Class<?> target, Class<? extends Annotation>... annotations) {
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
