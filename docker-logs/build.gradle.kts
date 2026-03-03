dependencies {
    implementation(project(":core"))

    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6PlatformLauncher)
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
    testImplementation(libs.junit6JupiterParams)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)

}