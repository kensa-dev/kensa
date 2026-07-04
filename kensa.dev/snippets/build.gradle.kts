description = "Compile-checked source for the code snippets on kensa.dev — not published, never executed"

dependencies {
    testImplementation(project(":framework-junit5"))
    testImplementation(project(":framework-testng"))
    testImplementation(project(":framework-kotest"))
    testImplementation(project(":assertions-assertj"))
    testImplementation(project(":assertions-kotest"))
    testImplementation(project(":assertions-hamkrest"))
    testImplementation(project(":docker-logs"))
    testImplementation(project(":kotest-test-support"))
    testImplementation(project(":kotest-test-support-xml"))
    testImplementation(project(":kotest-test-support-json"))

    testImplementation(platform(libs.junit5Bom))
    testImplementation(libs.junit5JupiterApi)
    testImplementation(libs.junit5JupiterParams)
    testImplementation(libs.testng)

    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.kotestFrameworkEngine)
    testImplementation(libs.kotestRunnerJunit5)

    testImplementation(libs.assertJCore)
    testImplementation(libs.hamkrest)
}

// The snippets are a compile-time contract only. They mix JUnit, TestNG and
// Kotest in one source set and exist to fail the build when an API change
// breaks a documented example — not to execute.
tasks.test {
    enabled = false
}
