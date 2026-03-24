package dev.kensa

import org.junit.jupiter.engine.Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME
import org.junit.platform.engine.DiscoverySelector
import org.junit.platform.engine.discovery.ClassSelector
import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder
import org.junit.platform.launcher.core.LauncherFactory
import org.junit.platform.launcher.listeners.SummaryGeneratingListener
import org.junit.platform.launcher.listeners.TestExecutionSummary
import java.lang.reflect.Method

object KensaTestExecutor {

    fun executeTest(clazz: Class<*>, method: Method) = executeKensaTest(false, selectMethod(clazz, method))

    fun executeTests(vararg classes: Class<*>): TestExecutionSummary =
        executeKensaTest(false, *classes.map<Class<*>, ClassSelector> { selectClass(it) }.toTypedArray<ClassSelector>())

    fun executeAllTestsIn(clazz: Class<*>) = executeKensaTest(false, selectClass(clazz))

    fun executeAllTestsInParallelIn(clazz: Class<*>) = executeKensaTest(true, selectClass(clazz))

    fun executeKensaTest(parallel: Boolean, vararg selectors: DiscoverySelector): TestExecutionSummary {
        val request = LauncherDiscoveryRequestBuilder.request()
            .selectors(*selectors)
            .configurationParameter(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, parallel.toString())
            .build()

        val summaryListener = SummaryGeneratingListener()
        LauncherFactory.create().execute(request, summaryListener)
        return summaryListener.summary
    }
}
