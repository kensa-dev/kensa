<h2 class="github">Changelog</h2>

### v0.8.14

Fixes:
  - **Fixture factory calls inside a `fixtures[...]` subscript render as a single value.** `fixtures[productFor(provideType)]` (the documented `@Fixture` factory-call syntax) previously leaked the literal word "fixtures" before the fixture value, because only the inner factory call was recognised. The whole `fixtures[factory(args)]` span, including any trailing navigation, is now recognised as one fixture-factory expression, so the accessor is absorbed.
  - **Parameterised test header hover no longer truncates.** The invocation header showed the full parameterised display name through a native `title` hover, which browsers cap at roughly 1024 characters, clipping long names mid-string. It now uses the built-in tooltip, which wraps and scrolls with no cap; the collapsed "N params" chip gained a hover listing the parameters, and long parameter names in the expanded table now show a hover too (#165).

### v0.8.13

Fixes:
  - **`@RenderedValue` identifier as a `fixtures[...]` / `outputs[...]` subscript key.** A rendered parameter or field used inside a fixtures or outputs subscript, such as `fixtures[productFor(provideType)]` or `outputs[trackingId]`, crashed the source parser and rendered the whole statement as "Could not parse this statement". The parser now keeps its expression stack balanced for these nested rendered identifiers (#163).
  - **`Named` parameterized-test arguments show their label.** JUnit unwraps `Named.of("label", value)` to the raw payload before Kensa sees the invocation, so the parameter table showed the payload instead of the label. Kensa now recovers the label positionally from the test display name and applies it to opaque payloads, falling back to the raw value when the argument count does not match the parsed parameters (#162).
  - **Opaque lambda and builder arguments render by type name.** Lambda and builder parameter values that fell through to `Object.toString()` produced non-deterministic noise like `MyTest$$Lambda/0x...@6f77685` in parameter tables. They now render as their functional interface simple name (for example `Function1`, or a custom SAM name), with `lambda` as a fallback. Registered value renderers still take precedence.

Performance:
  - **Fewer test-explorer re-renders in large reports.** Explorer rows no longer re-render on every selection or URL change, because only System-View rows subscribe to the router now. This cuts per-click main-thread work in reports with many test rows.

### v0.8.12

Fixes:
  - **`@ExpandableSentence` with value class parameters.** The Kotlin compiler mangles the JVM name of a function that takes a value class parameter (`someAction` becomes `someAction-abc123`), so Kensa could not find the method and reported the test as unparseable. Thanks to Michael Orr (#160).
  - **`@RenderedValue` with value class parameters or return types.** The same name mangling meant the captured invocation was recorded under the mangled JVM name and never matched the parsed sentence, so the rendered value was silently dropped.
  - **Array parameter types in parameterized tests.** Tests declaring array parameters — `Array<Pair<String, String>>` and other `Array<T>` (including nested, variance and nullable components), primitive arrays (`IntArray`, ...), Java arrays (`String[]`), `vararg` params — previously failed with `Did not find method declaration for test method`; plain `Float`/`Short`/`Byte`/`Char` params now match too. Array values also render like lists (`[(a, b)]`) everywhere instead of `Object.toString()` identity hashes (#161).

Performance:
  - **No reflective method lookup on instrumented calls.** The compiler-plugin hooks now record invocations by source name directly instead of deriving a `java.lang.reflect.Method` on every `@ExpandableSentence` / `@RenderedValue` call.

### v0.8.11

New features:
  - **Multi-assertion `thenEventually` / `thenContinually` blocks.** Both polling forms now accept a block of `then` / `and` assertions evaluated in parallel within a single polling window — `thenEventually` locks in each assertion independently as it starts passing, `thenContinually` requires all assertions to hold on every tick. Single failures are rethrown as-is; multiple failures are aggregated with the rest suppressed. Available in the Kotest and Hamkrest bridges.

Fixes:
  - **`ReplaceSentence` and `Ignore` hints in Java test sources.** The Java lexer now recognises the documented `/*+ ... */` hint form (it previously only matched javadoc-style comments, which then failed the prefix check), and the Java body parser now emits `Ignore` hints, matching the Kotlin parser (#151).

### v0.8.10

New features:
  - **`@Fixture` factory functions.** Annotate a function in a `FixtureContainer` with `@Fixture("Key")` and call it inline via `fixtures[myFixture(arg)]`; the compiler plugin rewrites its no-name `fixture { }` to inject the key and arguments, giving each `(key, args)` a distinct memoized identity that renders as a fixture token.

Fixes:
  - **Chained reference to a parameterised `@RenderedValue` method.** `productFor(p).stringValue` now renders the captured value instead of the call words / `null` (#149).
  - **Top-level `@RenderedValue` function.** Resolves the file-facade FQN (honouring `@file:JvmName`), fixing a `ClassNotFoundException` (#150).

### v0.8.9

New features:
  - **Null-safe paths in rendered values.** Rendered chained paths now accept the `?.` safe-call operator — `order?.customer?.name`, `fixtures[X]?.foo`, `outputs("k")?.bar` — which were previously dropped from value substitution (#144).
  - **Test parameters rendered through registered renderers.** Parameter values, the parameterised test description shown in the test header, and the suite search index now all honour registered value renderers, and each parameter is rendered exactly once (no more double-rendering when a `String` renderer is registered) (#145).
  - **Parameter-derived fixtures.** A `parameterFixture("key", from = "paramName") { … }` (Kotlin) / `createParameterFixture(...)` (Java) is registered by name — so it resolves in `fixtures[…]` interpolation — and derives its value per invocation from a named parameterised-test argument, seeded before the test body so it's usable both in the test and in the rendered sentence. Secondary fixtures can derive from one (#148).

Changed:
  - **Consistent lowercase `null` rendering.** Captured null values now render as lowercase `null` everywhere, matching the source-literal `null` keyword and the UI's null styling (previously an inconsistent `NULL`, which made an actual null display as a quoted string) (#146).

Fixes:
  - **No ConcurrentModificationException in sequence diagrams under parallel execution.** `SequenceDiagramFactory` is now safe when tests run in parallel (#142).
  - **`dataOnly` is authoritative over `outputDir` in site mode.**

### v0.8.8

New features:
  - **Suite-wide fixture search.** Modifier-click (find by fixture name) or right-click (find by value or name) any fixture token in a report to search the whole suite. Matches dock in a resizable, non-modal panel grouped project → class → method → invocation, auto-revealing the active result in the left explorer and highlighting it in violet across sentences and interaction payloads, landing on the exact invocation. Backed by a new per-source `search-index.json` — a value-anywhere index over rendered sentences, givens/outputs and interaction payloads, with short, boolean and small-numeric values filtered out as noise — emitted in both data-only and full output modes.

### v0.8.7

Changed:
  - **Built against Kotlin 2.4.0.**. Kensa now compiles against Kotlin 2.4.0 and requires consumers to be on Kotlin 2.4.0; projects still on Kotlin 2.3.x should stay on the previous Kensa release. Context parameters are stable in 2.4.0, so the deprecated `-Xcontext-parameters` flag has been dropped from the build.

### v0.8.6

New features:
  - **Expression-bodied `@ExpandableSentence` and test functions now render.** A function written with an expression body — `fun theDetails() = arrayOf(aField of "John", ...)` — previously produced an empty expandable popup, because an expression body emits no statement for the source parser to turn into a sentence. Expression bodies now render through the same path as block bodies. When the whole body is a single wrapping call (`arrayOf(...)`, `listOf(...)`, or any `builder(args)`) the call itself is stripped so only its contents render — a matcher/element list reads as a clean list without the surrounding builder. Lambda-delegating bodies (`= with(context) { ... }`, `= test { ... }`) are untouched and keep rendering their lambda statements; block bodies are unchanged.
  - **`/*+ Ignore */` source hint.** A `/*+ Ignore */` comment on its own line drops the rendered tokens on the next source line from the report sentence; `/*+ Ignore:n */` drops the next `n` lines. It works anywhere in a test or `@ExpandableSentence` body — a manual way to hide distracting plumbing (a wrapping `return arrayOf(` line, a gnarly builder chain) from the sentence without changing the test. Complements the existing `/*+ ReplaceSentence: ... */` hint.

### v0.8.5

Changed:
  - **`primary` participant now renders as the leftmost participant.** Previously `primary.participant(...)` was only emitted as an empty-diagram fallback for marker-only tests — configuring `primary.actor("SUT")` alongside other participants required re-declaring "SUT" via `participant("SUT")` to actually see it. `SequenceDiagramFactory` now prepends the primary's line to the participants list whenever it is set, so `primary` means what it reads as: the leftmost participant. When the same name is already declared (top-level or inside a `box { }`) the primary is suppressed so the explicit declaration wins and keeps its position. [Docs](https://kensa.dev/docs/api/configuration#sequence-diagrams).
  - **Test headers cap inline issue badges.** A test card header now renders at most three `@Issue` badges inline; any beyond that fold into a `+N more` popover listing the full set in a scrollable grid, so long issue lists no longer push the header layout around. Badges still link to the issue tracker, and the popover trigger does not toggle the test card.
  - **Tidier parameterised invocation headers.** Long backend display names now truncate to a single line (full text on hover) instead of wrapping and growing the header box, and a leading JUnit-style `[N] ` index prefix is stripped from the name since the card already carries a `#N` badge (a bare `[123]` with no trailing space — e.g. a Kotest list `toString` — is left intact).
  - **Sidebar test leaves read as leaves.** Test rows are inset past the absent-chevron gutter and the leaf diamond's stroke is thickened, so individual tests no longer blend in with the container folders above them.

Performance:
  - **Parsing no longer serialises on hash-bin collisions.** The per-method and per-class parser caches moved off `ConcurrentHashMap.computeIfAbsent`, which holds the bin lock for the entire duration of a (potentially slow) parse and blocks unrelated threads whose keys land in the same bin. They now use a `CompletableFuture`-based memoize that reserves the slot and runs the parse outside the lock, so concurrent first-time parses of different classes/methods proceed in parallel.
  - **Lower retained heap from cached parse trees.** `MethodDeclarationContext` now severs the parsed method body from its parent chain, so a cached `ParsedMethod` no longer pins the entire enclosing AST in memory.

Fixes:
  - **No report written when no tests ran.** `writeAllResults` forced the lazy `ResultWriter` even with an empty test set, which recreated (wiped) the output directory and wrote an empty report — destroying any report from a previous run. It now returns early when no test containers were recorded, leaving the output directory untouched on a zero-test run. Covers every framework adapter (JUnit 5/6, TestNG, Kotest), which all funnel through the same call.
  - **Breadcrumb no longer nests `<li>` inside `<li>`.** The package breadcrumb in the detail header rendered its separator as an `<li>` inside the item `<li>`, tripping React's `validateDOMNesting` warning. The separator is now a sibling of the item under the list.

### v0.8.4

New features:
  - **Sidebar tree auto-expands while filtering.** Typing in the test filter now reveals matches inside user-collapsed folders without touching the persisted shape — clear the filter and your collapse/expand state is restored exactly as it was. Builds on the v0.8.3 expand/collapse toolbar.

Fixes:
  - **Collapse-all now collapses every folder, not just project roots.** The `⌥+[` toolbar button only added per-source project roots to the collapsed set; package nodes were built dynamically per-render and never reached the expansion state, so opening a collapsed root revealed every nested package already expanded. The package tree is now materialised before expansion state sees it, matching IntelliJ behaviour: collapse-all → opening the root shows only its top-level entries; expand-all opens every folder.
  - **Persisted tree state is bounded.** `kensa-tree-collapsed` in localStorage now caps at 5000 entries, dropping the oldest on load. Prevents unbounded growth in long-lived workspaces where folder ids churn (heavy package refactors, frequent `packageDisplay` mode switches).
  - **All Kensa modules now appear in the BOM.** Modules added after the BOM's last refresh were missing from `kensa-bom`, so importing the BOM didn't constrain their versions.

### v0.8.3

New features:
  - **Sidebar tree expand/collapse toolbar.** Two icon buttons live inline with the Test Explorer group label: expand-all (`⌥+]`) and collapse-all (`⌥+[`). Expansion state persists to localStorage so the tree's open/closed shape survives reload. Newly-seen folder ids that aren't yet in storage stay expanded by default — no surprises after a build adds packages.
  - **Parameterised test display.** Invocation card headers no longer truncate parameter lists. A hybrid summary picks one of three shapes per invocation: the backend-supplied `displayName` when present (e.g. JUnit's `[1] arg1, arg2`); an inline comma-join when there are ≤3 parameters and the joined string fits in 80 characters; or a `N params` count chip otherwise. When the header had to drop information the expanded card body grows a structured parameters table; when the header already listed everything the table is suppressed to avoid redundancy. Above the per-invocation cards a cross-invocation matrix renders whenever a test has ≥2 invocations and every invocation carries ≥2 parameters — clicking a row scrolls to and expands the matching card. Values flow through a small client-side classifier that colours numbers / booleans / `null` / strings / JSON code-style.

Fixes:
  - **Console banner now prints an absolute output path.** The "Kensa Output:" line emitted at the end of a run is what IntelliJ turns into a clickable hyperlink — but only when the path is fully qualified. Relative `outputDir` values (common under `@KensaTest` YAML config or any `konfigure { outputDir = Path(...) }` with a relative argument) silently failed to link. Resolved to absolute at print time so the hyperlink works regardless of how `outputDir` was supplied.

### v0.8.2

New features:
  - **Framework-native tags surfaced in reports.** JUnit `@Tag`, Kotest `@Tags`, and TestNG `@Test(groups = ...)` now flow through to the HTML report as badges on each test (alongside `@Issue` badges) and as a multi-select cloud in the sidebar with OR-logic filtering. Click a badge to filter by that tag; Cmd/Ctrl/Shift-click to add to the current selection.
  - **`sequenceDiagram { }` Kotlin DSL** on `Configuration` for declaring participants, boxes, title, hide-unlinked, and a fallback identity. Declaration order is left-to-right on the rendered diagram. Each participant call (`participant`, `actor`, `boundary`, `control`, `entity`, `database`, `collections`, `queue`) returns a handle so `.withColour(...)` and `.withAlias(...)` chain inline. `box(title) { ... }` wraps nested participants. [Docs](https://kensa.dev/docs/api/configuration#sequence-diagrams).
  - **`primary.<type>(name)` fallback identity.** A test that captures only dividers (`SD-MARKER` / `==Something==`) or has no participants previously produced markup PlantUML couldn't recognise as a sequence diagram — falling into the error path that pulls in QR-code (`zxing`) classes stripped from the shadow JAR. Configure `sequenceDiagram { primary.actor("User") }` to inject a single participant for these cases. Only emitted when no participants are declared and no real arrow interactions are captured.

Fixes:
  - **UI no longer caches `loadJson` / `loadText` fetches.** Browser-side requests for `indices.json`, `results/*.json`, and source-file text now go out with `cache: 'no-store'` so a regenerated report is picked up on reload without a hard refresh.
  - **Late-applied sequence-diagram config is now observed by the factory.** When `Kensa.konfigure { ... }` runs from a companion-object `init` block (a common pattern — e.g., your own JUnit extension) it may execute *after* `KensaLifecycleManager.initialise(...)` has already constructed `SequenceDiagramFactory`. Previously the factory captured a reference to the old `var umlDirectives` list, so a subsequent `umlDirectives = listOf(...)` reassignment was invisible to it — the rendered diagram had no `participant` declarations and PlantUML laid out participants from interaction order alone. The factory now reads from the shared list at render time, so post-init reassignment via the deprecated setter (or the new `sequenceDiagram { }` block) is honoured.

Deprecated:
  - `Configuration.umlDirectives` — replaced by the `sequenceDiagram { }` block. The legacy setter still works but clears the directives list and invalidates any retained participant handle from a prior `sequenceDiagram { }` block. Java consumers can keep using it until a richer Java-side API lands.

### v0.8.1

New features:
  - **Spring Boot starter** — two new modules (`kensa-spring-boot-starter`, `kensa-spring-boot-starter-web`) wire Kensa into Spring Boot tests with zero manual configuration: `@KensaTest` boots the application context with Kensa registered as a JUnit Jupiter extension and configures the runtime from `kensa.*` properties in `application.yml`. The `-web` module auto-configures `HandlerInterceptor` / `ClientHttpRequestInterceptor` / `ExchangeFilterFunction` beans that capture HTTP interactions for the sequence diagram. Override any capture point by registering a bean with the canonical name (`kensaHandlerInterceptor`, `kensaClientHttpRequestInterceptor`, `kensaExchangeFilterFunction`) to plug in a party-aware variant. Spring deps are `compileOnly` — non-Spring projects on `kensa-core` see no transitive Spring dependency. [Docs](https://kensa.dev/docs/integrations/spring-boot-starter).

Changed:
  - `@ExpandableSentence` bodies are now parsed lazily — the parser runs only on first access, so unused expandable methods incur no parse cost.
  - **JUnit framework adapters — softer version pin.** `kensa-framework-junit5` and `-junit6` previously consumed the `junit-bom` at `implementation` scope, which propagated as a hard runtime dep and made Kensa's JUnit version a floor for consumers. The BOM is now `compileOnly`; the published POM still carries it as `<scope>import</scope>` in `<dependencyManagement>` (so suggested versions are visible to Maven consumers) but is no longer a runtime dep, letting Spring Boot's dependency-management plugin and similar version-managers win cleanly. `junit-jupiter-api`, `junit-jupiter-params`, and `junit-platform-launcher` still ship transitively at `implementation` scope, so consumer classpaths are unchanged. The dead `junit-jupiter-engine` declaration was removed (the adapter's listener uses `junit-platform-engine` types brought in via the launcher).
  - **TestNG framework adapter — `testng` is now `compileOnly`.** `kensa-framework-testng` no longer ships TestNG transitively. TestNG users already declare `testng` explicitly in their build, so the practical impact is nil; the win is that the adapter no longer imposes a TestNG version on consumers.

Fixes:
  - Remove `-Xexplicit-backing-fields` again (re-introduced unintentionally in 0.8.0). Downstream Kotlin projects no longer need `-Xskip-prerelease-check` to consume kensa-core.
  - **Per-source component diagrams in site mode.** The HTML UI was loading every per-source `aggregateComponentDiagram` from each `sources/<id>/indices.json` but then only using the *first* source's diagram as a global System View — every other source's architecture was silently dropped. Site mode now renders one System View per source, surfaced as a "System View" entry inside each source's tree root in the sidebar. Single-sourceset projects are unaffected (one source → one entry).
  - `kensa-framework-junit5`: per-class `results/*.json` files now write correctly on JUnit Jupiter < 5.13. The `CloseableTestContainer` that hooks end-of-class teardown used `java.io.Closeable`, which the Jupiter `ExtensionContext.Store` only honours on 5.13+; on 5.12 (forced by Spring Boot 3.5.x) the close callback never fired and test detail files were silently dropped. Switched to `ExtensionContext.Store.CloseableResource`, which is honoured by every 5.x version.

### v0.8.0
New features:
  - **Kotest Field Assertion DSL** — three new modules (`kensa-kotest-test-support`, `-xml`, `-json`) providing a `MatcherField<T, R>` interface and composable, named-field matchers for JSON and XML payloads. Extension functions `of` / `matching` / `withListOf` / `withSetOf` / `toMatcher` produce standard `Matcher<T?>` values that compose with `then(collector, matcher)`. Failure messages are auto-prefixed with the field's description. [Docs](https://kensa.dev/docs/field-assertion-dsl).
  - **Hamkrest Field Assertion DSL** — three new modules (`kensa-hamkrest-test-support`, `-xml`, `-json`) providing a `MatcherField<T, R>` interface and composable, named-field matchers for JSON and XML payloads. Extension functions `of` / `matching` / `withListOf` / `withSetOf` / `toMatcher` produce standard `Matcher<T?>` values that compose with `then(collector, matcher)`. Failure messages are auto-prefixed with the field's description. [Docs](https://kensa.dev/docs/field-assertion-dsl).
  - **Phrasing sugar** in both flavours — `with`, `thatHas` (single + vararg), `thatIs`, `shouldHaveAll` — for assertions that read like English at the call-site. [Docs](https://kensa.dev/docs/field-assertion-dsl/core#phrasing-sugar).
  - `@RenderedValueWithHint` integration for field DSL — JSONPointer / XPath surfaced as on-hover hints in the report. [Docs](https://kensa.dev/docs/field-assertion-dsl/report-rendering).
  - **Site mode** for HTML reports — runtime, UI, and CLI integration. [Docs](https://kensa.dev/docs/build-plugins/site-mode).
  - **Experimental UI test framework** with Playwright and Selenium adapters (`framework-playwright`, `framework-selenium`, plus `-junit5` / `-junit6` variants). [Docs](https://kensa.dev/docs/ui-testing/overview).
  - **Component diagram** view. Shows all interactions between components in a test suite. [Docs](https://kensa.dev/docs/component-diagrams).
  - **UI** Click-to-filter passed / failed / disabled badges on the sidebar footer.
  - **UI** Scrollable expandable popups.
  - Missing `andEventually` overloads on the kotest assertion entry points.

Changed:
  - CLI updated for site mode; warns on Kensa-version drift between the CLI and the report it serves. [Docs](https://kensa.dev/docs/cli).
  - Local builds now default the version to `snapshot-version.txt`.

Fixes:
  - `getFailingLine` choosing the wrong line number.
  - Default-mode `@ExpandableRenderedValue` rendering.
  - Kotest matcher double-execute inside `andEventually`.

Breaking:
  - Legacy UI removed (deprecated in 0.7.0; `Configuration.uiMode` and `KensaConfigurator.withUiMode(...)` are gone, along with the `UiMode` enum).

### v0.7.1
  - Fix #132: don't clear sidebar search on Escape while a dialog is open
  - Fix #131: match issue ids exactly, not as substrings
  - Build uses Gradle 9.5.0
  - Kotlin 2.3.21

### v0.7.0
Breaking — deprecated APIs removed:
  - `@NestedSentence` annotation removed — use `@ExpandableSentence`
  - `Givens` / `GivensBuilder` / `ActionUnderTest` / `StateExtractor` and the deprecated `given(GivensBuilder)` & `whenever(ActionUnderTest)` overloads removed
  - Deprecated `KensaTest` overloads removed from the JUnit 5 / JUnit 6 adapters
  - `WithAssertJ` and all `*StateExtractor` helper classes removed (AssertJ module)
  - `WithHamcrest` removed; `KotestThen` pruned of deprecated entry points
  - Enum values `Section.Buttons` and `Tab.Givens` removed
  - `SuppressParseErrors` annotation removed — the parser now recovers from errors automatically, making suppression unnecessary

Breaking — renamed parameter `extractor` → `collector` on assertion entry points. If you were calling these with named parameters (`then(extractor = ...)`), update call sites.

Deprecations:
  - `Configuration.uiMode` and `KensaConfigurator.withUiMode(...)` — Modern is now the only supported UI; Legacy UI and the `UiMode` enum are scheduled for removal in 0.8.0.

New features:
  - `/*+ ReplaceSentence: ... */` hint — a comment placed before any test statement replaces its rendered sentence with the hint text. Supports value interpolation via `{expr}` (fields, fixtures, outputs, chained calls). Discouraged as a long-term pattern; intended as a short-term workaround when Kensa fails to parse a test due to unsupported syntax — please [raise an issue](https://github.com/kensa-dev/kensa/issues) if you hit a parser gap.
  - Parse errors no longer fail the build. The parser recovers and continues, and failed sentences are rendered inline with an error marker in the report.
  - Modern UI is now the default `UIMode`.
  - `@RenderedValueWithHint` is now hierarchy-aware — a directive declared on a supertype or interface applies to every subtype unless a more specific directive is declared. Avoids having to repeat the annotation for each member of a sealed hierarchy.

Fixes:
  - Revert `-Xexplicit-backing-fields` compile option. Kensa no longer requires downstream projects to set `-Xskip-prerelease-check`.
  - Source file lookup now works when the sources come from the Gradle cache or a local Maven repository and support fat sources with `-sources` classifier.
  - More robust recognition of `fixtures[...]`, `outputs(...)`, and chained property/method calls in test sources, backed by dedicated regex tests.

### v0.6.7
  - Add annotation `SuppressParseErrors` to allow temporary bypass of parsing errors. Also added instructional sentences in test output.
  - Fix an issue where log indices were not indexed correctly for consecutive log lines.

### v0.6.6
  - Synchronising with IntelliJ plugin. Allow the plugin to expand individual tests when clicking on the gutter icon. 

### v0.6.5
  - Fix issue where failed tests using ExpandableSentence or ExpandableRenderedValue would not render correctly.
  - CMD-F binding in Modern UI removed as it was conflicting with default browser search. Replaced with '/' to focus search field.
  - Modern UI test explorer now orders packages before tests. 
  - (Experimental) Custom tabs now support image rendering (for upcoming UI testing functionality).
  - Allow regex to be passed to `IndexedLogFileQueryService` to enable log delimiter lines to more easily matched
  - Regression - ExpandableSentence defined via Sources annotation were not recognised properly.
  - Add 'FixtureSuite' and `WithFixtureSuite` alongside new fixture rendering capability using `fixtures { MyFixtureFx }` in tests. Intended to reduce
    the number of import boilerplate in tests that use a large number of fixtures. (Thanks to Ryan Taplin for the suggestion)
  - Improve recognition of `EpandableSentence` that use lambda parameters. 

### v0.6.4
  - Adding overloads for fixtures when using function references.
  - Allowing inline notes to be rendered inside Kotlin tests that are expression functions.

### v0.6.3
  - Fixing issue where legacy tests would not render due to changes in the json format.

### v0.6.2
  - Relocating more PlantUml classes which were causing `VerifyError` in some applications that also use PlantUML.

### v0.6.1
  - Fixture table — parent/child highlighting, hierarchy display, corrected ordering & family grouping. Thanks to Jamie Redding!
  - Fixtures can now be specified as highlighted
  - Claude Code Skill
  - Modern UI overhaul — IBM Plex fonts, larger base size, GWT keyword colour-coding, styled notes, section separators
  - Notes annotation — class-level markdown notes rendered above test list; supports bold/italic/links & tables, internal cross-suite navigation (#ClassName.methodName)
  - Clickable package breadcrumbs — clicking a package segment updates the search query
  - Sentence line numbers — exceptions/failures now highlighted in the report
  - Keyboard scrolling — issue/state picker list scrolls with arrow keys
  - URL entry — entering the app with a query URL now selects the first matching test
  - Protected phrases honoured in test display names
  - Custom tabs — HTML content can use secure allow-same-origin iframe
  - Dependency pruning — antlr4 → antlr4-runtime (saves ~7MB); kotlinCompilerEmbeddable marked compileOnly; PlantUML JAR shadowed; assertion libs promoted to api() scope
  - Kotlin 2.3.20 — includes fix for downstream compiler plugin errors
  - Kotest 6.1 — version bump + test fix
  - Gradle 9.4.1
  - Experimental Kotest and TestNG support (tests pending)
  - Report date & Kensa version shown in the modern UI
  - Disabled tests & package display fixes
  - JUnit 6 BOM no longer leaks into assertion dependencies — Tim W
  - Sequence diagram CSS tweaks (light & dark mode improvements)
  - Hamcrest/Hamkrest modules aligned with Kotest module structure

### v0.6.0
  - Experimental support for JUnit 6
    * Breaking dependency change: Now need to specify junit dependency as either `dev.kensa:kensa-framework-junit5` or `dev.kensa:kensa-framework-junit6` 

### v0.5.47
  - Modern UI improvements:
    Select test and expand first matching method when filtering.
    Honour the display name

### v0.5.46
  - Improve registration of log services for Modern UI (Thanks to Paul Reynolds)

### v0.5.45
  - Modern UI improvements:
    Keep focus in the search field after 'Escape' clear
    Remove package icon.
    Add keyboard navigation to issue and state pickers.
    Handle method level issue filtering 
    Allow removing the state/issue badge by clicking on the x
    Dial down bar colours a bit

### v0.5.44
  - Fix issue where some report lines would not be indented correctly
  - Modern UI improvements

### v0.5.43 
- Adding `scope` support for tabs, alowing single log tab content to be shown across multiple tests.
- Add RawLogFileQueryService for displaying raw files without indexing.
- Add ability to jump to and expand a specific test via the 'method' query parameter.
- DockerCliLogQueryService now using Regex for matching.

### v0.5.42 
- LogFileQueryService now using Regex for matching.
- Improvements & fixes to Custom tab rendering in modern UI.

### v0.5.41 
- Experimental LogFileQueryService now using `startsWith` for delimiter line matching. 

### v0.5.40 
- Experimental support for custom tabs in Modern UI. Includes ability to query log files
  and docker logs.

### v0.5.39
- Modern UI improvements.
- Fixing issue where `ExpandableSentence` annotation was not working properly.

### v0.5.38
- Improving how nested sentences are matched with source code. Now using full type matching.
- Fixing autoOpen of tabs in modern UI.
- Adding overflow popover panel for issues in test header.

### v0.5.37 - cancelled

### v0.5.36
- Adding support for `Notes` in test output.
  Kensa will recognise `///` as a note when it is placed before a Given/When/Then keyword. 
  Notes will also be recognised at the end of a line (immediately following a `}` or `)`)
  
  Thanks to Michael Orr.
- Introduce `ExpandableRenderedValue` to allow function markers to be expanded to show the function return value in a table.
  This is useful for functions that return collections.
- Introduce `RenderedValueWithHint` to allow fields to be rendered with a hint tooltip.
  This can be used to provide additional information about a field, such as a JsonPath or XPath.
- Deprecate `NestedSentence` in favour of `ExpandableSentence`
- Introduce early access version of a new UI.
  Requires use of a web server to view the output. Use IDEA's or TeamCity's built in server, or use Kensa's own CLI to spin up a server.

### v0.5.35
- Updates for compatibility with Kotlin 2.3.0

### v0.5.34
- Kotest version bump for compatibility

### v0.5.33
- Fix regression with parameterised tests that contained nested sentences.

### v0.5.32
- Improvements to how the Kotlin compiler plugin handles extension functions and context parameters in nested sentences.

### v0.5.31
- Allow nested sentences to be defined in Interfaces when using the Kotlin compiler plugin.
- Honour whitespace when displaying field and parameter values (Thanks to Michael Orr)

### v0.5.30
- Introduce Kotlin compiler plugin to support advanced rendering for @RenderedValue & @NestedSentence annotated functions.
- Fix issue where parameters were not being recognised when closing bracket was on a new line. (Thanks to Neil Massey)

### v0.5.29 - cancelled
### v0.5.28 - cancelled
### v0.5.27 - cancelled

### v0.5.26
- Fix issue with plural ProtectedPhrases
- Fix issue with Kensa agent and Kotlin 2.2.20 (ByteBuddy Advice problems)
- Fixing more layout issues with nested sentences
- Bump to Kotlin 2.2.20

### v0.5.25
- Correcting the rendering of nested sentences.
- Improving the rendering & parsing of RenderedValue functions with parameters.

### v0.5.24
- Tweak alignment of nested sentence floating and embedded block.
- Experimental support for rendering return values of methods/functions that take parameters. Currently only matches on method/function name.  Requires kensa-agent.
- Fix for fake plurals of ProtectedPhrases.

### v0.5.23
- Support for Kotest 6.

### v0.5.22
- Fix issue with Java record rendering
- Adding used fixtures tab the test output
- Adding ability to search captured interactions
- Improvements to captured interactions ui (with placeholder for raw rendering)
- Experimental CLI to spin up a local server for viewing the test output
- Protected phrases now recognise their plural form

### v0.5.21
- Adding better support for Kotlin backticked test names.
- Fix issue with Nested Sentences with parameters that are function calls with parameters.

### v0.5.20
- Fix an issue with Nested Sentences not parsing correctly when top level expression

### v0.5.19
- Adding a copy button to rendered captured output

### v0.5.18
- Removing some unused deprecations
- Improve exception handling for `onMatch` in ThenSpec
- Adding Adoptabot example project to be used in docs
- Adding more docs

### v0.5.17
- Preserve whitespace in value block of tables
- Add CapturedOutputs to GivensContext to allow chaining of givens steps

### v0.5.16
- AssertJ with StateCollectors
- TestContextUtil with Action & StateCollector

### v0.5.15
- Captured Outputs can be rendered by the string key as well as typed name

### v0.5.14 - Interim testing release ** DO NOT USE **
- Rethinking fixtures. No longer mutable.
- Introduce CapturedOutputs

### v0.5.13
- Improved nested sentences:
  - Parameters are no longer bound to share the parent test arguments (requires *kensa-agent*)
  - Arguments are rendered on the test output as part of the call
  - Arguments are rendered within the nested sentence as expected
- Add ability to define private fixtures to use as parents of other secondary fixtures.
- Add some syntactic sugar for SetupSteps.

### v0.5.12 - cancelled

### v0.5.11
- Version bumps
- Colour fix for disabled tests
- Intermittent test fail fix due to concurrency

### v0.5.10
- WithFixtures as receiver for ThenSpec

### v0.5.9
- Allow secondary fixtures to have multiple primary dependencies

### v0.5.8
- Chained calls after Fixture expressions are resolved

### v0.5.7
- Make TestContextFixtures use a getter

### v0.5.6
- Adding FixturesParameterResolver
- Adding fixtures as parameter to `onMatch` for `ThenSpecWithFixtures`
- Allow SecondaryFixtures to be parents of other fixtures.

### v0.5.5
- Adding `hasValue` function to Fixtures.
- TestContext should be a class not an object.

### v0.5.4
- Fix ui issues. 
- Introduce *WithFixtures suffix interfaces for GivensBuilder, ActionUnderTest & StateExtractor.
- Revert GivensBuilder, ActionUnderTest & StateExtractor to remove Fixtures (for backwards compatibility).

### v0.5.3 - cancelled

### v0.5.2
- Fixing parsing issue 

### v0.5.1
- Introduce Bom.
- Fixes for Bulma typography.
- Lenient value reflection.

### v0.5.0
- Breaking changes: Dependencies changed in prep for supporting other test frameworks:
  - Now need to choose dependency `dev.kensa:kensa-framework-junit` and one of:
  - `dev.kensa:kensa-assertions-kotest`
  - `dev.kensa:kensa-assertions-hamcrest`
  - `dev.kensa:kensa-assertions-hamkrest`
  - `dev.kensa:kensa-assertions-assertj`
- Breaking changes: `JavaKensaTest` has been removed - use `KensaTest` from JUnit framework dependency
- Breaking changes: `KotlinKensaTest` has been removed - use `KensaTest` from JUnit framework dependency
- Breaking changes: Introduce `fixtures` parameter to GivensBuilder & ActionUnderTest
- Breaking changes: Rename annotation `SentenceValue` to `RenderedValue`
- Breaking changes: Rename annotation `ScenarioHolder` to `RenderedValueHolder`
- Breaking changes: Remove annotation `Scenario` (replaced by call chaining capability)
- Breaking changes: Remove interface GivensWithInteractionsBuilder
- Rendered values will now follow chained calls eg `myField.myPropery`
- Introduce `Fixtures`. Tests now have access to a built-in fixture container with custom factories. Fixture values will be rendered by default in test output

### v0.5.0-SNAPSHOT
- Drop webpack. Introduce Vite. UI layout changes. Parsing Changes. Test Fixtures.   

### v0.4.34
- #45 : Ignore type arguments when building sentences
- #43 : Display the test class's package as part of the header (thanks to Michael Orr)

### v0.4.33
- #39,#40  : Introduce `flattenOutputPackages` configuration flag (defaults to `false`), to allow package folder structure to be maintained or flattened. Ability to use custom test or index writers is now removed.
- #41  : JUnit's `ParameterizedTest` annotation is correctly handled with the `name` template becoming the test's display name. 

### v0.4.32
- #38  : Allow setup to specify which Tab to open (Givens, Parameters, Captured Interactions or Sequence Diagram)

### v0.4.31
- #37  : WIP - Introduce SetupSteps

### v0.4.30
- #35  : Quick 'fix' for Sources annotation use with Java  

### v0.4.29
- #34  : Support Java 20 Grammar (Fix scenarios) 

### v0.4.28
- #34  : Support Java 20 Grammar (Fix scenarios)
- #35  : Add Source annotation and allow source files to be specified (WIP) 

### v0.4.27
- #34  : Support Java 20 Grammar (Fix noisy output)

### v0.4.26
- #34  : Support Java 20 Grammar

### v0.4.25
- #33  : Add sequence diagram filtering via clickable actor names (thanks to Michael Orr)

### v0.4.24
- #32  : Replace HighlightedIdentifier with ProtectedPhrase

### v0.4.23
- #31  : Add floating headers to sequence diagrams (thanks to Michael Orr)

### v0.4.22 - NOT PUBLISHED

### v0.4.21
- #29  : Allow configuration of `initialDelay` & `interval` for `thenEventually` functions.
- #30  : Add additional parsing events for Kotlin tokens (`Boolean`, `Char`, `null` & hexadecimal numbers)

### v0.4.20
- #27  : Update UI to use modern ReactJS function components. Allow filtering by issue. Remove showOnSequenceDiagram for RenderedAttributes as they were legacy. 

### v0.4.19
- #27  : Fixes for issue rendering 

### v0.4.18
- Bump some versions
- #27  : Allow manual filtering of test classes by issue number

### v0.4.17 - NOT PUBLISHED

### v0.4.16 - NOT PUBLISHED

### v0.4.15 - NOT PUBLISHED

### v0.4.14
- #25  : Wide sequence diagrams now scrollable (thanks to **michaelomichael**)

### v0.4.13
- #24  : Ensure extractor is called only once in KotestThen#then

### v0.4.12
- #23  : UmlDirective to hide unlinked participants

### v0.4.11
- #22  : Fix stacktrace overflow issues.

### v0.4.10
- #21  : Fix stacktrace overflow issues.

### v0.4.9
- #20  : Introduce `ParameterizedTestDescription` annotation. Tidy.

### v0.4.8
- #19  : More experimentation with Kotest.  Support `thenContinuously`. Introduce `ThenSpec`

### v0.4.7
- #19  : Starting some work on `thenEventually` improvements for Kotlin/Kotest

### v0.4.6
- #18  : Improve handling of expression function tests
- #17  : Introduce ScenarioHolder

### v0.4.5
- #15  : Add timestamp to KensaMap entries to ease sequence diagram order issues
- #14  : Complete thenEventually for Kotest

### v0.4.4
- #13  : Add rendering of equals & arrow operator to Kotlin tests

### v0.4.3
- #13  : Add rendering of equals & arrow operator to Kotlin tests

### v0.4.2
- #8  : Improve rendering of attributes and values - changes after feedback

### v0.4.1
- #8  : Improve rendering of attributes and values - changes after feedback

### v0.4.0
- #10 : Allow rendering of collections using the registered renderer of the content objects
- #8  : Improve rendering of attributes and values

### v0.3.1
- #6 : Synchronise read access for KensaMap.
- Bump Kotlin & Kotest versions

### v0.3.0
- #4 : Allow Java AssertJ tests to specify own assertion 
- #5 : No longer exposing Kotlin's Function1 via Java api in WithAssertJ.java 

### v0.2.6
- Clean up 

### v0.2.5
- #3 : Reintroduce some capability of customising the test output via `TestWriter` & `IndexWriter` interfaces 

### v0.2.4
- #2 : Reinstate transparent background & white group box in sequence diagram 

### v0.2.3
- #3 : Improve total output rendering time by making individual test containers render their output when JUnit closes their context 

### v0.2.2
- Fix issue with `when` keywords not being recognised
- Various NPM updates

### v0.2.1
- Kotlin 1.7.10
- Kotest Assertions

### v0.2.0
- First version for maven central
