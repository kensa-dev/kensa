dependencies {
    implementation(project(":core"))

    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitPlatformLauncher)
    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.junitJupiterParams)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)

}