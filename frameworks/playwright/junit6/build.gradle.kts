description = "Kensa Playwright UI testing for JUnit 6"

// Shared sources with framework-playwright-junit5; canonical copy in ../junit-common.
sourceSets.main { kotlin.srcDir("../junit-common/src/main/kotlin") }

dependencies {
    api(project(":framework-playwright"))
    api(project(":framework-uitesting-junit6"))
}
