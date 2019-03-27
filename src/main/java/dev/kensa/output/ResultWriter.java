package dev.kensa.output;

import dev.kensa.Kensa;
import dev.kensa.context.TestContainer;
import dev.kensa.util.IoUtil;

import java.util.List;

public class ResultWriter {

    private final Kensa.Configuration configuration;

    public ResultWriter(Kensa.Configuration configuration) {
        this.configuration = configuration;
    }

    public void write(List<TestContainer> containers) {
        IoUtil.recreate(configuration.outputDir());

        configuration.outputStyle().write(containers, configuration);

        IoUtil.copyResource("/kensa.js", configuration.outputDir());

        System.out.println("\nKensa Output :\n" + configuration.outputDir().resolve("index.html"));
    }
}