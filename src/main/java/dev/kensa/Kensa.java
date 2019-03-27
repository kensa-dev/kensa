package dev.kensa;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import dev.kensa.output.OutputStyle;
import dev.kensa.output.template.Template;
import dev.kensa.render.Renderer;
import dev.kensa.render.Renderers;
import dev.kensa.render.diagram.directive.UmlDirective;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public final class Kensa {

    private static final String KENSA_OUTPUT_ROOT = "kensa.output.root";
    private static final Kensa KENSA = new Kensa();

    public static Kensa configure() {
        return KENSA;
    }

    static Configuration configuration() {
        return KENSA.configuration;
    }

    private final Configuration configuration = new Configuration();

    private Kensa() {
    }

    public Kensa withOutputDir(Path dir) {
        configuration.outputDir = dir;

        return this;
    }

    public <T> Kensa withRenderer(Class<T> klass, Renderer<? extends T> renderer) {
        configuration.renderers.add(klass, renderer);

        return this;
    }

    public Kensa withOutputStyle(OutputStyle outputStyle) {
        configuration.outputStyle = outputStyle;

        return this;
    }

    public class Configuration {
        private final PebbleEngine pebbleEngine;

        private Path outputDir;
        private Renderers renderers;
        private List<UmlDirective> umlDirectives;
        private OutputStyle outputStyle;

        private Configuration() {
            this.outputDir = Paths.get(System.getProperty(KENSA_OUTPUT_ROOT, System.getProperty("java.io.tmpdir")), "kensa-output");
            this.renderers = new Renderers();
            this.umlDirectives = new ArrayList<>();
            this.outputStyle = OutputStyle.MultiFile;
            this.pebbleEngine = new PebbleEngine.Builder().autoEscaping(false).loader(new ClasspathLoader()).build();
        }

        public Path outputDir() {
            return outputDir;
        }

        public Renderers renderers() {
            return renderers;
        }

        List<UmlDirective> umlDirectives() {
            return umlDirectives;
        }

        public OutputStyle outputStyle() {
            return outputStyle;
        }

        public Template createTemplate(String path, Template.Mode mode) {
            return createTemplate(outputDir.resolve(path), mode);
        }

        public Template createTemplate(Path path, Template.Mode mode) {
            return new Template(outputDir.resolve(path), mode, pebbleEngine);
        }
    }
}
