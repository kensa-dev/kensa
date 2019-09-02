package dev.kensa.util;

import dev.kensa.*;
import dev.kensa.function.Unchecked;
import dev.kensa.parse.CachingFieldAccessor;
import dev.kensa.parse.CachingScenarioMethodAccessor;
import dev.kensa.parse.ParameterAccessor;
import org.assertj.core.util.Strings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Reflect {

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
                    Object returnValue = field.get(target);

                    if (returnValue instanceof Supplier) {
                        returnValue = ((Supplier) returnValue).get();
                    }

                    return returnValue;
                }))
                .map(type::cast)
                .orElse(null);
    }

    public static <T> T invokeMethod(Object target, String name, Class<T> requiredType) {
        Objects.requireNonNull(target);

        Method method = findMethod(target.getClass(), parameterlessMethodNamed(name))
                .orElseThrow(() -> new KensaException(String.format("Unable to find method [%s] on class [%s]", name, target.getClass().getName())));

        try {
            method.setAccessible(true);
            return requiredType.cast(method.invoke(target));
        } catch (Exception e) {
            throw new KensaException(String.format("Unable to invoke method [%s] on class [%s]", name, target.getClass().getName()), e);
        }
    }

    public static ParameterAccessor interestingParametersOf(Method method, List<NamedValue> parameters) {
        Parameter[] methodParameters = method.getParameters();

        Set<NamedValue> namedValues = new HashSet<>();

        for (int i = 0; i < methodParameters.length; i++) {
            Parameter parameter = methodParameters[i];
            if (parameter.isAnnotationPresent(SentenceValue.class)) {
                namedValues.add(parameters.get(i));
            }
        }

        return new ParameterAccessor(namedValues);
    }

    public static Set<NamedValue> nestedSentencesOf(Object target) {
        Set<NamedValue> methods = new HashSet<>();
        Class<?> aClass = target.getClass();

        while (aClass != Object.class) {
            Method[] declaredMethods = aClass.getDeclaredMethods();
            Arrays.stream(declaredMethods)
                  .filter(method -> method.isAnnotationPresent(NestedSentence.class))
                  .forEach(method -> methods.add(new NamedValue(method.getName(), method)));
            aClass = aClass.getSuperclass();
        }

        return methods;
    }

    public static CachingFieldAccessor interestingFieldsOf(Object target) {
        Set<String> fieldNames = new HashSet<>();
        Class<?> aClass = target.getClass();

        while (aClass != Object.class) {
            Field[] declaredFields = aClass.getDeclaredFields();
            Arrays.stream(declaredFields)
                  .filter(field -> field.isAnnotationPresent(Highlight.class) || field.isAnnotationPresent(SentenceValue.class))
                  .forEach(field -> fieldNames.add(field.getName()));
            aClass = aClass.getSuperclass();
        }

        return new CachingFieldAccessor(target, fieldNames);
    }

    public static CachingScenarioMethodAccessor scenarioAccessorFor(Object target) {
        Set<String> fieldNames = new HashSet<>();
        Class<?> aClass = target.getClass();

        while (aClass != Object.class) {
            Field[] declaredFields = aClass.getDeclaredFields();
            Arrays.stream(declaredFields)
                  .filter(field -> field.isAnnotationPresent(Scenario.class))
                  .forEach(field -> fieldNames.add(field.getName()));
            aClass = aClass.getSuperclass();
        }

        return new CachingScenarioMethodAccessor(target, fieldNames);
    }

    public static Set<NamedValue> highlightedFieldsOf(Object target) {
        Set<NamedValue> highlightedFields = new HashSet<>();
        Class<?> aClass = target.getClass();

        while (aClass != Object.class) {
            Field[] declaredFields = aClass.getDeclaredFields();
            Arrays.stream(declaredFields)
                  .filter(field -> field.isAnnotationPresent(Highlight.class))
                  .forEach(field -> {
                      Highlight highlight = field.getAnnotation(Highlight.class);

                      highlightedFields.add(new NamedValue(Strings.isNullOrEmpty(highlight.value()) ? field.getName() : highlight.value(), field.getName()));
                  });
            aClass = aClass.getSuperclass();
        }
        return highlightedFields;
    }

    private static Optional<Method> findMethod(Class<?> target, Predicate<Method> matcher) {
        for (Class<?> clazz = target; clazz != null && clazz != Object.class; clazz = clazz.getSuperclass()) {
            for (Method method : clazz.getDeclaredMethods()) {
                if (matcher.test(method)) {
                    return Optional.of(method);
                }
            }

            for (Class<?> iClazz : clazz.getInterfaces()) {
                Optional<Method> iMethod = findMethod(iClazz, matcher);
                if (iMethod.isPresent()) {
                    return iMethod;
                }
            }
        }

        return Optional.empty();
    }

    private static Predicate<Method> parameterlessMethodNamed(String name) {
        return method -> method.getName().equals(name) && method.getParameterCount() == 0;
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

    private static Set<Method> findAnnotatedMethods(Class<?> target, Set<Method> methods, Class<? extends Annotation>[] annotations) {
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

    private static Predicate<Method> isAnnotatedWith(Class<? extends Annotation>[] annotations) {
        return m -> Arrays.stream(annotations).anyMatch(m::isAnnotationPresent);
    }

    private Reflect() {
    }
}
