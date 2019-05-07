package dev.kensa;

import dev.kensa.context.TestContainer;
import dev.kensa.output.ResultWriter;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.synchronizedList;

public class KensaExecutionContext implements ExtensionContext.Store.CloseableResource {

    private final ResultWriter resultWriter;
    private final List<TestContainer> containers = synchronizedList(new ArrayList<>());

    KensaExecutionContext(ResultWriter resultWriter) {
        this.resultWriter = resultWriter;
    }

    @Override
    public void close() {
        resultWriter.write(containers);
    }

    void register(TestContainer testContainer) {
        containers.add(testContainer);
    }
}