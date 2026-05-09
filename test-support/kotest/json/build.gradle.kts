description = "JSON field-based assertion utilities for Kotest"

dependencies {
    api(project(":kotest-test-support"))
    api(libs.jacksonDatabind)

    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6PlatformLauncher)
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
}
