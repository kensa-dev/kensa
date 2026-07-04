// Snippet source for kensa.dev/docs/api/configuration.md — Kotlin tabs
package apidocs

import dev.kensa.Kensa
import dev.kensa.PackageDisplay
import dev.kensa.render.ListRendererFormat
import dev.kensa.render.ValueRenderer
import dev.kensa.sentence.Acronym
import dev.kensa.sentence.ProtectedPhrase
import dev.kensa.sequenceDiagram
import dev.kensa.withRenderers
import quickstart.Money
import java.net.URI
import kotlin.io.path.Path

class MoneyRenderer : ValueRenderer<Money> {
    override fun render(value: Money): String = "${value.currency} ${value.amount}"
}

fun entryPoints() {
    Kensa.konfigure {
        outputDir = Path("build/kensa-output")
        issueTrackerUrl = URI("https://github.com/my-org/my-repo/issues/").toURL()
    }
}

fun output() {
    Kensa.konfigure {
        outputDir = Path("build/kensa-output")
        packageDisplay = PackageDisplay.HideCommonPackages
    }
}

fun sentenceParsing() {
    Kensa.konfigure {
        protectedPhrases(ProtectedPhrase("credit score"))
        acronyms(Acronym.of("API", "Application Programming Interface"))
    }
}

fun renderers() {
    Kensa.configure()
        .withValueRenderer(Money::class, MoneyRenderer())
        .withListRendererFormat(ListRendererFormat(separator = " | ", prefix = "(", postfix = ")"))

    Kensa.konfigure {
        withRenderers {
            valueRenderer<Money> { money -> "${money.currency} ${money.amount}" }
        }
    }
}

fun sequenceDiagrams() {
    Kensa.konfigure {
        sequenceDiagram {
            title("Order placement")
            actor("User")
            participant("Frontend")
            box("Backend") {
                participant("Orchestration")
                database("OrderStore")
            }
            queue("Events")
            hideUnlinked()
        }
    }
}

fun sequenceDiagramExtras() {
    Kensa.konfigure {
        sequenceDiagram {
            actor("Operations").withColour("#LightBlue").withAlias("Ops")
            box("Backend", colour = "#LightYellow") {
                participant("Orchestration")
                database("OrderStore")
            }
            title("Order placement", "Happy path")
            primary.actor("SUT").withColour("#LightGreen")
            hideUnlinked()
        }
    }
}

fun sourceLocations() {
    Kensa.konfigure {
        sourceLocations = listOf(Path("src/test/kotlin"))
    }
}
