package dev.kensa

import dev.kensa.context.TestContainer
import dev.kensa.context.TestContainerFactory
import dev.kensa.context.TestContext
import dev.kensa.fixture.Fixtures
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class KensaContext(private val testContainerFactory: TestContainerFactory) {

    private val _testContainers = ConcurrentHashMap<Class<*>, TestContainer>()
    val testContainers: List<TestContainer> get() = _testContainers.values.toList()

    fun createTestContainer(testClass: Class<*>, displayName: String, commonBasePackage: String): TestContainer =
        testContainerFactory.createFor(testClass, displayName, commonBasePackage).also { _testContainers.getOrPut(testClass) { it } }

    fun startTestInvocation(testInstance: Any, testClass: Class<*>, testMethod: Method, arguments: List<Any?>, displayName: String, startTimeMs: Long, fixtures: Fixtures): UUID =
        _testContainers.getValue(testClass).startTestInvocation(testInstance, testMethod, arguments, displayName, startTimeMs, fixtures)

    fun endTestInvocation(testClass: Class<*>, testMethod: Method, testContext: TestContext, testId: UUID, executionException: Throwable?, endTimeMs: Long) {
        _testContainers.getValue(testClass).endTestInvocation(testMethod, testContext, testId, executionException, endTimeMs)
    }
}