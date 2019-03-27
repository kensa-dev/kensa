package dev.kensa.output;

import dev.kensa.Kensa;
import dev.kensa.context.TestContainer;

import java.util.List;
import java.util.function.BiConsumer;

public enum OutputStyle {

    SingleFile(new SingleFileWriter()),
    MultiFile(new MultiFileWriter()),
    Site(new SiteWriter());

    private final BiConsumer<List<TestContainer>, Kensa.Configuration> writer;

    OutputStyle(BiConsumer<List<TestContainer>, Kensa.Configuration> writer) {
        this.writer = writer;
    }

    public void write(List<TestContainer> containers, Kensa.Configuration configuration) {
        writer.accept(containers, configuration);
    }
}
