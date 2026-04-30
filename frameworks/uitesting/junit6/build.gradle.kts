description = "Kensa UI Testing — JUnit 6 adapter"

dependencies {
    api(project(":core"))
    api(project(":framework-uitesting"))
    api(project(":framework-junit6"))

    compileOnly(platform(libs.junit6Bom))
    compileOnly(libs.junit6JupiterApi)

    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
    testImplementation(libs.junit6PlatformTestKit)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
