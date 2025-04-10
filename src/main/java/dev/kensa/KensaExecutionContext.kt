package dev.kensa

import dev.kensa.context.TestContainer
import java.util.*
import java.util.Collections.synchronizedList

class KensaExecutionContext {
    private val _containers = synchronizedList(ArrayList<TestContainer>())
    val containers: List<TestContainer> get() = _containers

    fun add(testContainer: TestContainer) {
        _containers.add(testContainer)
    }
}