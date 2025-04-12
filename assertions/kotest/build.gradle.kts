dependencies {
    implementation(project(":core"))

    implementation(libs.kotlinCoroutines)
    implementation(libs.kotestAssertionsCoreJvm)

    testImplementation(libs.junitJupiterApi)
    testImplementation(libs.junitJupiterEngine)
    testImplementation(libs.kotlinCoroutinesTest)
    testImplementation(libs.mockitoKotlin)

}
