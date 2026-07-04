# Docs snippets

Source for the code examples on [kensa.dev](https://kensa.dev). One file per docs
page (see the header comment in each file). Compiled — never run — as the
`:doc-snippets` module of the main Gradle build, so an API change that breaks a
documented example fails `./gradlew check`.

Workflow: change the snippet file first, compile, then copy the relevant block
into the markdown fence in `kensa.dev/docs/`.

Note: pushes touching only `kensa.dev/**` skip the main build CI, so a
snippet-only change is verified locally, not on push.
