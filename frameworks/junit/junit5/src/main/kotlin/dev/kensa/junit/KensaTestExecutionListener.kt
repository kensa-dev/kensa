package dev.kensa.junit

import dev.kensa.context.KensaLifecycleManager
import dev.kensa.util.hasAnnotation
import org.junit.jupiter.api.Disabled
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan

class KensaTestExecutionListener : TestExecutionListener {

    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        KensaLifecycleManager.initialise(junit5Descriptor)
    }

    override fun executionSkipped(identifier: TestIdentifier, reason: String?) {
        val source = identifier.source.orElse(null) as? ClassSource ?: return
        val testClass = runCatching { Class.forName(source.className) }.getOrNull() ?: return
        if (!testClass.hasAnnotation<Disabled>()) return
        KensaLifecycleManager.current()?.skipClass(testClass, identifier.displayName)
    }

    override fun testPlanExecutionFinished(testPlan: TestPlan?) {
        KensaLifecycleManager.current()?.writeAllResults()
    }
}
