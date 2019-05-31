package dev.kensa.output;

import dev.kensa.Kensa;
import dev.kensa.context.TestContainer;
import dev.kensa.util.IoUtil;

import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

public class ResultWriter {

    private final Supplier<Kensa.Configuration> configurationSupplier;

    public ResultWriter(Supplier<Kensa.Configuration> configurationSupplier) {
        this.configurationSupplier = configurationSupplier;
    }

    public void write(List<TestContainer> containers) {
        Kensa.Configuration configuration = configurationSupplier.get();
        Path outputDir = configuration.outputDir();
        OutputStyle outputStyle = configuration.outputStyle();

        IoUtil.recreate(outputDir);

        outputStyle.write(containers, configuration);

        IoUtil.copyResource("/kensa.js", outputDir);

        System.out.println("\nKensa Output :\n" + outputDir.resolve("index.html"));
    }
}