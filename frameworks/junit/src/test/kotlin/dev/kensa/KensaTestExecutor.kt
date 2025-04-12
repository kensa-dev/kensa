package dev.kensa

import org.junit.jupiter.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.junit.platform.testkit.engine.EngineExecutionResults
import org.junit.platform.testkit.engine.EngineTestKit
import java.lang.reflect.Method

internal object KensaTestExecutor {

    fun executeTest(clazz: Class<*>, method: Method) = executeKensaTest(false, selectMethod(clazz, method))

    fun executeTests(vararg classes: Class<*>): EngineExecutionResults =
        executeKensaTest(false, *classes.map<Class<*>, ClassSelector> { selectClass(it) }.toTypedArray<ClassSelector>())

    fun executeAllTestsIn(clazz: Class<*>) = executeKensaTest(false, selectClass(clazz))

    fun executeAllTestsInParallelIn(clazz: Class<*>) = executeKensaTest(true, selectClass(clazz))

    fun executeKensaTest(parallel: Boolean, vararg classSelectors: DiscoverySelector): EngineExecutionResults =
        EngineTestKit
            .engine("junit-jupiter")
            .selectors(*classSelectors)
            .configurationParameter(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, parallel.toString())
            .execute()
}