package dev.kensa;

import org.junit.jupiter.engine.Constants;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineExecutionResults;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

final class KensaTestExecutor {

    static void executeKensaTest(Class<?> clazz, Method method) {
        executeKensaTest(false, selectMethod(clazz, method));
    }

    static EngineExecutionResults executeTests(Class<?>... classes) {
        return executeKensaTest(false, Stream.of(classes).map(DiscoverySelectors::selectClass).toArray(ClassSelector[]::new));
    }

    static void executeAllTestsIn(Class<?> clazz) {
        executeKensaTest(false, selectClass(clazz));
    }

    static void executeAllTestsInParallelIn(Class<?> clazz) {
        executeKensaTest(true, selectClass(clazz));
    }

    static EngineExecutionResults executeKensaTest(Boolean parallel, DiscoverySelector... classSelectors) {
        return EngineTestKit
                .engine("junit-jupiter")
                .configurationParameter(Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, parallel.toString())
                .selectors(classSelectors)
                .execute();
    }
}