description = "Kensa Selenium UI testing for JUnit 6"

// Shared sources with framework-selenium-junit5; canonical copy in ../junit-common.
sourceSets.main { kotlin.srcDir("../junit-common/src/main/kotlin") }

dependencies {
    api(project(":framework-selenium"))
    api(project(":framework-uitesting-junit6"))
}
