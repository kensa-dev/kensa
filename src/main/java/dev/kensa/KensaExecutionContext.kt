package dev.kensa

import dev.kensa.context.TestContainer
import dev.kensa.output.ResultWriter
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import java.util.*
import java.util.Collections.synchronizedList

class KensaExecutionContext internal constructor(private val resultWriter: ResultWriter) : CloseableResource {
    private val containers = synchronizedList(ArrayList<TestContainer>())

    override fun close() {
        resultWriter.write(sortedContainers())
    }

    fun register(testContainer: TestContainer) {
        containers.add(testContainer)
    }

    private fun sortedContainers(): Set<TestContainer> {
        return TreeSet(Comparator.comparing { o: TestContainer -> o.testClass.qualifiedName!! }).apply {
            this.addAll(containers)
        }
    }
}