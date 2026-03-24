description = "Hamcrest assertion helpers for Kensa"

dependencies {
    implementation(project(":core"))

    api(libs.hamcrestCore)
    api(libs.awaitilityKotlin)

    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6PlatformLauncher)
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.mockitoKotlin)
}