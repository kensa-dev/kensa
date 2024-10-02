<h2 class="github">Changelog</h2>

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
