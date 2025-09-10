<h2 class="github">Changelog</h2>

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
