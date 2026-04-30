description = "Kensa UI Testing — JUnit 5 adapter"

dependencies {
    api(project(":core"))
    api(project(":framework-uitesting"))
    api(project(":framework-junit5"))

    compileOnly(platform(libs.junit5Bom))
    compileOnly(libs.junit5JupiterApi)

    testImplementation(platform(libs.junit5Bom))
    testImplementation(libs.junit5JupiterApi)
    testImplementation(libs.junit5JupiterEngine)
    testImplementation(libs.junit5PlatformTestKit)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
