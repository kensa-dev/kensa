description = "Field-based assertion utilities for Kotest"

dependencies {
    api(platform(libs.kotestBom))
    api(libs.kotestAssertionsCoreJvm)

    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6PlatformLauncher)
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
    testImplementation(libs.mockitoKotlin)
}
