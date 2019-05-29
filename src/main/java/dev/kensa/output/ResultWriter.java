package dev.kensa.output;

import dev.kensa.Kensa;
import dev.kensa.context.TestContainer;
import dev.kensa.util.IoUtil;

import java.nio.file.Path;
import java.util.List;

public class ResultWriter {

    private final Kensa.Configuration configuration;
    private final Path outputDir;
    private final OutputStyle outputStyle;

    public ResultWriter(Kensa.Configuration configuration) {
        this.configuration = configuration;
        this.outputDir = configuration.outputDir();
        this.outputStyle = configuration.outputStyle();
    }

    public void write(List<TestContainer> containers) {
        IoUtil.recreate(outputDir);

        outputStyle.write(containers, configuration);

        IoUtil.copyResource("/kensa.js", outputDir);

        System.out.println("\nKensa Output :\n" + outputDir.resolve("index.html"));
    }
}