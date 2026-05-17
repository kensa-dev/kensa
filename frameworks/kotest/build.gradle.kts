description = "Kensa for Kotest"

dependencies {
    api(project(":core"))
    compileOnly(project(":antlr"))
    implementation(libs.antlr)
    implementation(libs.kotlinReflect)

    compileOnly(platform(libs.kotestBom))
    compileOnly(libs.kotestFrameworkEngine)
    compileOnly(libs.kotestRunnerJunit5)

    testImplementation(platform(libs.junit5Bom))
    testImplementation(libs.junit5JupiterApi)
    testImplementation(libs.junit5JupiterEngine)
    testImplementation(libs.junit5PlatformLauncher)

    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.kotestFrameworkEngine)
}


tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
