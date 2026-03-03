package dev.kensa.junit

import dev.kensa.util.findCommonPackage
import org.junit.platform.engine.support.descriptor.ClassSource
import org.junit.platform.launcher.TestExecutionListener
import org.junit.platform.launcher.TestIdentifier
import org.junit.platform.launcher.TestPlan

/**
 * Shares data between the Launcher's TestExecutionListener and
 * the KensaExtension.
 */
object TestPlanDetails {
    var commonBasePackage: String = ""
}

class KensaTestExecutionListener : TestExecutionListener {
    override fun testPlanExecutionStarted(testPlan: TestPlan?) {
        testPlan?.also {
            TestPlanDetails.commonBasePackage = determineCommonBasePackageFor(it)
        } 
    }

    private fun determineCommonBasePackageFor(testPlan: TestPlan): String = 
        testPlan
            .roots
            .flatMap(testPlan::getChildren)
            .filter(TestIdentifier::isContainer)
            .filter { it.source.isPresent }
            .map { it.source.get() }
            .filterIsInstance<ClassSource>()
            .map { it.javaClass.packageName }
            .toList()
            .let(::findCommonPackage)
}