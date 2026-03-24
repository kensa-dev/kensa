package dev.kensa.testng

import dev.kensa.context.KensaLifecycleManager
import dev.kensa.context.TestContainer
import org.testng.IClassListener
import org.testng.ISuiteListener
import org.testng.ITestClass
import org.testng.ITestContext
import org.testng.ITestListener
import org.testng.ITestResult
import org.testng.ISuite
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class KensaTestNgListener : ITestListener, IClassListener, ISuiteListener {

    private val containers = ConcurrentHashMap<Class<*>, TestContainer>()
    private val currentInvocationId = ThreadLocal<UUID?>()

    // ISuiteListener

    override fun onStart(suite: ISuite) {
        KensaLifecycleManager.initialise(testNgDescriptor)
    }

    override fun onFinish(suite: ISuite) {
        KensaLifecycleManager.current()?.writeAllResults()
    }

    // IClassListener

    override fun onBeforeClass(testClass: ITestClass) {
        val manager = KensaLifecycleManager.current() ?: return
        val realClass = testClass.realClass
        manager.beforeClass(realClass, realClass.simpleName)?.let { container ->
            containers[realClass] = container
        }
    }

    override fun onAfterClass(testClass: ITestClass) {
        val container = containers.remove(testClass.realClass) ?: return
        KensaLifecycleManager.current()?.writeTestResult(container)
    }

    // ITestListener

    override fun onTestStart(result: ITestResult) {
        val manager = KensaLifecycleManager.current() ?: return
        val testClass = result.testClass.realClass
        val method = result.method.constructorOrMethod.method
        manager.beforeTest(testClass, method)
        currentInvocationId.set(
            manager.startInvocation(
                result.instance,
                testClass,
                method,
                result.parameters.toList(),
                result.name,
            )
        )
    }

    override fun onTestSuccess(result: ITestResult) = endInvocation(result, null)

    override fun onTestFailure(result: ITestResult) = endInvocation(result, result.throwable)

    override fun onTestSkipped(result: ITestResult) = endInvocation(result, null)

    override fun onTestFailedButWithinSuccessPercentage(result: ITestResult) = endInvocation(result, result.throwable)

    override fun onStart(context: ITestContext) = Unit

    override fun onFinish(context: ITestContext) = Unit

    private fun endInvocation(result: ITestResult, exception: Throwable?) {
        val manager = KensaLifecycleManager.current() ?: return
        manager.endInvocation(
            result.testClass.realClass,
            result.method.constructorOrMethod.method,
            currentInvocationId.get(),
            exception,
        )
        currentInvocationId.remove()
    }
}
