description = "Kensa for TestNG"

dependencies {
    api(project(":core"))
    compileOnly(project(":antlr"))
    implementation(libs.antlrRuntime)
    implementation(libs.kotlinReflect)
    compileOnly(libs.testng)

    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
    testImplementation(libs.junit6PlatformLauncher)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.testng)
}
