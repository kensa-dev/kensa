package dev.kensa.output;

import com.eclipsesource.json.JsonValue;
import dev.kensa.Kensa;
import dev.kensa.KensaException;
import dev.kensa.context.TestContainer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.kensa.output.json.JsonTransforms.toJsonWith;
import static dev.kensa.output.template.Template.Mode.Site;
import static dev.kensa.output.template.Template.asIndex;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class SiteWriter implements BiConsumer<List<TestContainer>, Kensa.Configuration> {

    @Override
    public void accept(List<TestContainer> containers, Kensa.Configuration configuration) {
        var indexTemplate = configuration.createTemplate("index.html", Site);

        for (var container : containers) {
            var path = configuration.outputDir().resolve(container.getClass().getName() + ".json");
            writeToFile(path).accept(container.transform(toJsonWith(configuration.renderers())));

            indexTemplate.addIndex(container, asIndex());
        }

        indexTemplate.write();
    }

    private static Consumer<JsonValue> writeToFile(Path path) {
        return jv -> {
            try {
                jv.writeTo(Files.newBufferedWriter(path, UTF_8, CREATE_NEW));
            } catch (IOException e) {
                throw new KensaException("Unable to write Json to file", e);
            }
        };
    }
}
