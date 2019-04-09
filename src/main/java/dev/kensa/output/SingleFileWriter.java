package dev.kensa.output;

import dev.kensa.Kensa;
import dev.kensa.context.TestContainer;
import dev.kensa.output.template.Template;

import java.util.List;
import java.util.function.BiConsumer;

import static dev.kensa.output.template.Template.Mode.SingleFile;
import static dev.kensa.output.template.Template.asIndex;
import static dev.kensa.output.template.Template.asJsonScript;

public class SingleFileWriter implements BiConsumer<List<TestContainer>, Kensa.Configuration> {
    @Override
    public void accept(List<TestContainer> containers, Kensa.Configuration configuration) {
        Template template = configuration.createTemplate("index.html", SingleFile);

        for (TestContainer container : containers) {
            template.addJsonScript(container, asJsonScript(configuration.renderers()));
            template.addIndex(container, asIndex());
        }

        template.write();
    }
}