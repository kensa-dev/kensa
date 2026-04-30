description = "Playwright BrowserDriver implementation for Kensa UI testing"

dependencies {
    api(project(":framework-uitesting"))
    api(libs.playwrightJava)

    testImplementation(platform(libs.junit5Bom))
    testImplementation(libs.junit5JupiterApi)
    testImplementation(libs.junit5JupiterEngine)
    testRuntimeOnly(libs.junit5PlatformLauncher)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.mockitoKotlin)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
