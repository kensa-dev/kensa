description = "Kotest assertion helpers for Kensa"

dependencies {
    implementation(project(":core"))

    implementation(platform(libs.junitBom))
    implementation(libs.kotestAssertionsCoreJvm)
    implementation(libs.kotlinCoroutines)

    testImplementation(libs.junitPlatformLauncher)
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.kotlinCoroutinesTest)
    testImplementation(libs.mockitoKotlin)

}
