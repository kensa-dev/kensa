description = "Kotest assertion helpers for Kensa"

dependencies {
    implementation(project(":core"))

    implementation(platform(libs.kotestBom))
    api(libs.kotestAssertionsCoreJvm)
    api(libs.kotlinCoroutines)

    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6PlatformLauncher)
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
    testImplementation(libs.kotlinCoroutinesTest)
    testImplementation(libs.mockitoKotlin)
}
