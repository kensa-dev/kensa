description = "Kensa Playwright UI testing for JUnit 5"

// Shared sources with framework-playwright-junit6; canonical copy in ../junit-common.
sourceSets.main { kotlin.srcDir("../junit-common/src/main/kotlin") }

dependencies {
    api(project(":framework-playwright"))
    api(project(":framework-uitesting-junit5"))
}
