package dev.kensa.output.template;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import dev.kensa.KensaException;
import dev.kensa.context.TestContainer;
import dev.kensa.render.Renderers;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static dev.kensa.output.json.JsonTransforms.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.StandardOpenOption.CREATE;

public class Template {

    public enum Mode {
        SingleFile,
        MultiFile,
        TestFile,
        Site
    }

    private final List<Index> indices = new ArrayList<>();
    private final Mode mode;
    private final URL issueTrackerUrl;
    private final PebbleTemplate template;
    private final List<JsonScript> scripts = new ArrayList<>();
    private final Path outputPath;

    private int indexCounter = 1;

    public Template(Path outputPath, Mode mode, URL issueTrackerUrl, PebbleEngine pebbleEngine) {
        this.mode = mode;
        this.outputPath = outputPath;
        this.issueTrackerUrl = issueTrackerUrl;
        this.template = pebbleEngine.getTemplate("pebble-index.html");
    }

    public void addIndex(TestContainer container, BiFunction<TestContainer, Integer, Index> factory) {
        indices.add(factory.apply(container, indexCounter++));
    }

    public void addJsonScript(TestContainer container, BiFunction<TestContainer, Integer, JsonScript> factory) {
        scripts.add(factory.apply(container, indexCounter));
    }

    public void write() {
        Map<String, Object> context = new HashMap<>();
        context.put("scripts", scripts);
        context.put("indices", indices);
        context.put("mode", mode.name());
        context.put("issueTrackerUrl", issueTrackerUrl == null ? "" : issueTrackerUrl.toString());
        write(context);
        this.scripts.clear();
        indices.clear();
    }

    private void write(Map<String, Object> context) {
        try {
            template.evaluate(Files.newBufferedWriter(outputPath, UTF_8, CREATE), context);
        } catch (IOException e) {
            throw new KensaException("Unable to write template", e);
        }
    }

    public static BiFunction<TestContainer, Integer, Index> asIndex() {
        return (container, index) -> container.transform(toIndexJson("test-result-" + index)
                                                                 .andThen(toJsonString())
                                                                 .andThen(Index::new)
        );
    }

    public static BiFunction<TestContainer, Integer, JsonScript> asJsonScript(Renderers renderers) {
        return (container, index) -> container.transform(toJsonWith(renderers)
                                                                 .andThen(toJsonString())
                                                                 .andThen(js -> new JsonScript("test-result-" + index, js))
        );
    }
}
