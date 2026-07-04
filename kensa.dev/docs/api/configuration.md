---
sidebar_position: 2
description: Complete reference for configuring Kensa — output directory, issue tracker URL, report layout, sentence parsing, custom renderers, and source locations.
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Configuration

Kensa is configured once, before your tests run. There are two styles — a Kotlin DSL and a fluent Java-friendly builder.

## Entry Points

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
// DSL style — preferred in Kotlin
Kensa.konfigure {
    outputDir = Path("build/kensa-output")
    issueTrackerUrl = URI("https://github.com/my-org/my-repo/issues/").toURL()
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
// Builder style — preferred in Java
Kensa.configure()
    .withOutputDir(Paths.get("build/kensa-output").toAbsolutePath())
    .withIssueTrackerUrl(URI.create("https://github.com/my-org/my-repo/issues/").toURL());
```

</TabItem>
</Tabs>

Configuration is typically placed in a test base class, a JUnit 5 `@BeforeAll`, or a Kotest `ProjectConfig`.

---

## Output

| Builder method | DSL property | Type | Default | Description |
|----------------|--------------|------|---------|-------------|
| `withOutputDir(path)` | `outputDir` | `Path` | system temp dir / `kensa-output` | Where HTML reports are written. The builder method requires an **absolute** path and appends a `kensa-output` leaf unless the path already ends with one; the DSL property assigns the path exactly as given |
| `withOutputDisabled()` | `isOutputEnabled = false` | `Boolean` | `true` | Suppress all report generation |
| `withFlattenOutputPackages(bool)` | `flattenOutputPackages` | `Boolean` | `false` | Simplify package paths in output filenames |
| `withPackageDisplayMode(mode)` | `packageDisplay` | `PackageDisplay` | `HideCommonPackages` | How package names appear in the report |
| `withPackageDisplayRoot(root)` | `packageDisplayRoot` | `String?` | `null` | Root package to strip when displaying package names |

**System properties** override defaults before configuration is applied:

| Property | Effect |
|----------|--------|
| `kensa.output.root` | Overrides the output directory root |
| `kensa.disable.output` | Set to any value to disable output |

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
Kensa.konfigure {
    outputDir = Path("build/kensa-output")
    packageDisplay = PackageDisplay.HideCommonPackages
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
Kensa.configure()
    .withOutputDir(Paths.get("build/kensa-output").toAbsolutePath())
    .withPackageDisplayMode(PackageDisplay.HideCommonPackages);
```

</TabItem>
</Tabs>

---

## Issue Tracker

Annotate tests with `@Issue("PROJ-123")` and Kensa will link to the issue in the report. Set the base URL here:

| Builder method | DSL property | Type | Description |
|----------------|--------------|------|-------------|
| `withIssueTrackerUrl(url)` | `issueTrackerUrl` | `URL` | Base URL — issue keys are appended directly |

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
Kensa.konfigure {
    issueTrackerUrl = URI("https://github.com/my-org/my-repo/issues/").toURL()
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
Kensa.configure()
    .withIssueTrackerUrl(URI.create("https://github.com/my-org/my-repo/issues/").toURL());
```

</TabItem>
</Tabs>

With the above URL, `@Issue("42")` links to `https://github.com/my-org/my-repo/issues/42`.

---

## Report Layout

| Builder method | DSL property | Type | Default | Description |
|----------------|--------------|------|---------|-------------|
| `withSectionOrder(vararg sections)` | `sectionOrder` | `List<Section>` | `[Tabs, Sentences, Exception]` | Order of report sections |
| `withAutoOpenTab(tab)` | `autoOpenTab` | `Tab` | `Tab.None` | Which tab is open by default |
| `withAutoExpandNotes(bool)` | `autoExpandNotes` | `Boolean` | `false` | Expand `@Notes` content automatically |
| `withTabSize(n)` | `tabSize` | `Int` | `4` | Code indentation width |
| `withSetupStrategy(strategy)` | `setupStrategy` | `SetupStrategy` | `SetupStrategy.Ungrouped` | How setup interactions appear in sequence diagrams |

**`Section` values:** `Tabs`, `Sentences`, `Exception`

**`Tab` values:** `CapturedInteractions`, `CapturedOutputs`, `Parameters`, `SequenceDiagram`, `None`

**`SetupStrategy` values:**

| Value | Effect |
|-------|--------|
| `Ungrouped` | Setup interactions shown inline, not grouped |
| `Grouped` | Setup interactions grouped into a labelled box |
| `Ignored` | Setup interactions hidden from the sequence diagram |

---

## Sentence Parsing

Kensa parses method names to build readable sentences. Extend the dictionary to handle domain-specific terms:

| Builder method | Description |
|----------------|-------------|
| `withProtectedPhrases(vararg phrases)` | Prevent a multi-word phrase from being split (e.g. `"credit score"`) |
| `withAcronyms(vararg acronyms)` | Register an acronym and its meaning (e.g. `Acronym.of("API", "Application Programming Interface")`) |
| `withKeywords(vararg keywords)` | Add custom BDD keywords beyond the defaults |

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
Kensa.konfigure {
    protectedPhrases(ProtectedPhrase("credit score"))
    acronyms(Acronym.of("API", "Application Programming Interface"))
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
Kensa.configure()
    .withProtectedPhrases(new ProtectedPhrase("credit score"))
    .withAcronyms(Acronym.of("API", "Application Programming Interface"));
```

</TabItem>
</Tabs>

---

## Custom Renderers

Register custom value or interaction renderers for types Kensa doesn't know how to display:

| Builder method | Description |
|----------------|-------------|
| `withValueRenderer(klass, renderer)` | Custom `ValueRenderer<T>` for a specific type |
| `withInteractionRenderer(klass, renderer)` | Custom `InteractionRenderer<T>` for sequence diagram content |
| `withListRendererFormat(format)` | Override the default `[a, b, c]` list format |

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
Kensa.configure()
    .withValueRenderer(Money::class, MoneyRenderer())
    .withListRendererFormat(ListRendererFormat(separator = " | ", prefix = "(", postfix = ")"))

// Or using the DSL extension:
Kensa.konfigure {
    withRenderers {
        valueRenderer<Money> { money -> "${money.currency} ${money.amount}" }
    }
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
Kensa.configure()
    .withValueRenderer(Money.class, money -> money.getCurrency() + " " + money.getAmount())
    .withListRendererFormat(new ListRendererFormat(" | ", "(", ")"));
```

</TabItem>
</Tabs>

---

## Sequence Diagrams

Configure how participants are declared and rendered in the sequence diagram. Declaration order in the `sequenceDiagram { }` block is the left-to-right order on the rendered diagram.

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
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
```

</TabItem>
<TabItem value="java" label="Java">

The `sequenceDiagram { }` DSL is Kotlin-only, but the underlying `SequenceDiagramConfiguration` methods are plain Java-callable. From Java, supply your own `KensaConfigurationProvider` (wired up via the `dev.kensa.ConfigurationProvider` system property) and call them directly:

```java
import dev.kensa.Configuration;
import dev.kensa.KensaConfigurationProvider;
import dev.kensa.SequenceDiagramConfiguration;

public class MyKensaConfiguration implements KensaConfigurationProvider {

    private final Configuration configuration = new Configuration();

    public MyKensaConfiguration() {
        SequenceDiagramConfiguration diagram = configuration.getSequenceDiagram();
        diagram.title("Order placement");
        diagram.actor("User");
        diagram.participant("Frontend");
        diagram.participant("Orchestration");
        diagram.database("OrderStore");
        diagram.queue("Events");
        diagram.hideUnlinked();
    }

    @Override
    public Configuration invoke() {
        return configuration;
    }
}
```

```kotlin title="build.gradle(.kts) — test task"
jvmArgs("-Ddev.kensa.ConfigurationProvider=com.example.MyKensaConfiguration")
```

With a custom provider, the returned `Configuration` is the one Kensa uses — set all other configuration on the same instance rather than via `Kensa.configure()`. Boxes (`box { }`) are only available through the Kotlin DSL.

</TabItem>
</Tabs>

### Participants

Eight participant kinds — each maps directly to a PlantUML participant type:

| DSL method | PlantUML output |
|------------|-----------------|
| `participant(name)` | `participant name` |
| `actor(name)` | `actor name` |
| `boundary(name)` | `boundary name` |
| `control(name)` | `control name` |
| `entity(name)` | `entity name` |
| `database(name)` | `database name` |
| `collections(name)` | `collections name` |
| `queue(name)` | `queue name` |

Each call returns a handle. Chain `.withColour(...)` and `.withAlias(...)` inline to style or re-label the participant:

```kotlin
sequenceDiagram {
    actor("Operations").withColour("#LightBlue").withAlias("Ops")
    participant("Orchestration")
}
```

A `Party` overload is provided for each method — if your test suite already defines `Party` constants, pass them directly: `participant(MyParty.Orchestration)`.

### Boxes

Group related participants under a labelled box. The box is rendered around the listed participants:

```kotlin
sequenceDiagram {
    box("Backend", colour = "#LightYellow") {
        participant("Orchestration")
        database("OrderStore")
    }
}
```

Nested boxes are not supported. Use sibling boxes at the top level.

### Title & Hide Unlinked

```kotlin
sequenceDiagram {
    title("Order placement", "Happy path")
    actor("User")
    participant("Unused")
    hideUnlinked()                 // omits participants that have no interactions
}
```

### Primary Participant

Mark a participant as **primary** to pin it as the leftmost participant in the diagram and to guarantee it is rendered even when a test captures only a divider (`SD-MARKER` value such as `==Setup==`) or no interactions at all — situations where PlantUML would otherwise fail to recognise the markup.

```kotlin
sequenceDiagram {
    primary.actor("SUT").withColour("#LightGreen")
    participant("OrderService")
    database("Ledger")
}
```

The primary is prepended to the participants emitted in the diagram, so a single declaration is enough — no need to also list the same name via `participant("SUT")`. If you do declare a participant with the same name elsewhere (top level or inside a `box { }`), the primary is suppressed so it is not double-emitted; your explicit declaration wins (and keeps its position).

`primary.<type>(name)` accepts the same eight participant kinds as the top-level methods and returns the same handle for `.withColour(...)` / `.withAlias(...)` chaining. Setting `primary` twice overwrites the previous value — only one primary is honoured.

### Replacement Semantics

Each `sequenceDiagram { }` block resets the prior configuration before applying its body:

```kotlin
Kensa.konfigure {
    sequenceDiagram { participant("Alpha") }
    sequenceDiagram { participant("Bravo") }     // Alpha is discarded
}
```

This matches the replacement behaviour of the deprecated `umlDirectives = listOf(...)` setter.

---

## Source Locations

If you use `@Sources` to parse helper classes referenced in tests, tell Kensa where to find source files:

| Builder method | DSL property | Type | Description |
|----------------|--------------|------|-------------|
| `withSourceLocations(vararg paths)` | `sourceLocations` | `List<Path>` | Paths to scan for `.kt` / `.java` source files |

<Tabs groupId="lang">
<TabItem value="kotlin" label="Kotlin">

```kotlin
Kensa.konfigure {
    sourceLocations = listOf(Path("src/test/kotlin"))
}
```

</TabItem>
<TabItem value="java" label="Java">

```java
Kensa.configure()
    .withSourceLocations(Path.of("src/test/java"));
```

</TabItem>
</Tabs>
