// Snippet source for kensa.dev/docs/api/configuration.md — Java tabs
package apidocs;

import dev.kensa.Kensa;
import dev.kensa.PackageDisplay;
import dev.kensa.render.ListRendererFormat;
import dev.kensa.sentence.Acronym;
import dev.kensa.sentence.ProtectedPhrase;
import quickstart.Money;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

class ConfigurationSnippets {

    void entryPoints() throws Exception {
        Kensa.configure()
                .withOutputDir(Paths.get("build/kensa-output").toAbsolutePath())
                .withIssueTrackerUrl(URI.create("https://github.com/my-org/my-repo/issues/").toURL());
    }

    void output() {
        Kensa.configure()
                .withOutputDir(Paths.get("build/kensa-output").toAbsolutePath())
                .withPackageDisplayMode(PackageDisplay.HideCommonPackages);
    }

    void sentenceParsing() {
        Kensa.configure()
                .withProtectedPhrases(new ProtectedPhrase("credit score"))
                .withAcronyms(Acronym.of("API", "Application Programming Interface"));
    }

    void renderers() {
        Kensa.configure()
                .withValueRenderer(Money.class, money -> money.getCurrency() + " " + money.getAmount())
                .withListRendererFormat(new ListRendererFormat(" | ", "(", ")"));
    }

    void sourceLocations() {
        Kensa.configure()
                .withSourceLocations(Path.of("src/test/java"));
    }
}
