description = "Kensa UI Testing — driver-agnostic abstractions"

dependencies {
    api(project(":core"))

    testImplementation(platform(libs.junit5Bom))
    testImplementation(libs.junit5JupiterApi)
    testImplementation(libs.junit5JupiterEngine)
    testRuntimeOnly(libs.junit5PlatformLauncher)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
