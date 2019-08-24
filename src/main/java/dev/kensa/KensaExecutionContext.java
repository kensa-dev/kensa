package dev.kensa;

import dev.kensa.context.TestContainer;
import dev.kensa.output.ResultWriter;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.*;

import static java.util.Collections.synchronizedList;

public class KensaExecutionContext implements ExtensionContext.Store.CloseableResource {

    private final ResultWriter resultWriter;
    private final List<TestContainer> containers = synchronizedList(new ArrayList<>());

    KensaExecutionContext(ResultWriter resultWriter) {
        this.resultWriter = resultWriter;
    }

    @Override
    public void close() {
        resultWriter.write(sortedContainers());
    }

    void register(TestContainer testContainer) {
        containers.add(testContainer);
    }

    private Set<TestContainer> sortedContainers() {
        SortedSet<TestContainer> sortedContainers = new TreeSet<>(Comparator.comparing(o -> o.testClass().getName()));
        sortedContainers.addAll(containers);

        return sortedContainers;
    }
}