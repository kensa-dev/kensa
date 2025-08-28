description = "Kotest assertion helpers for Kensa"

dependencies {
    implementation(project(":core"))

    implementation(platform(libs.kotestBom))
    implementation(libs.kotestAssertionsCoreJvm)
    implementation(libs.kotlinCoroutines)

    implementation(platform(libs.junitBom))
    testImplementation(libs.junitPlatformLauncher)
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.kotlinCoroutinesTest)
    testImplementation(libs.mockitoKotlin)

}
