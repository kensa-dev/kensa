description = "Kensa Selenium UI testing for JUnit 5"

// Shared sources with framework-selenium-junit6; canonical copy in ../junit-common.
sourceSets.main { kotlin.srcDir("../junit-common/src/main/kotlin") }

dependencies {
    api(project(":framework-selenium"))
    api(project(":framework-uitesting-junit5"))
}
