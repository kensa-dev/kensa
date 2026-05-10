description = "JSON field-based assertion utilities for Hamkrest"

dependencies {
    api(project(":hamkrest-test-support"))
    api(libs.jacksonDatabind)

    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6PlatformLauncher)
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
}
