dependencies {
    implementation(libs.http4kCore)
    implementation(libs.http4kJackson)
    implementation(libs.http4kKotest) {
        // Temporary until Http4k releases for Kotest 6.x
        exclude(group = "io.kotest")
    }

    testImplementation(libs.http4kClientOkHttp)
    testImplementation(libs.jacksonKotlin)

    // Temporary until Http4k releases for Kotest 6.x
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)

    implementation(platform(libs.junitBom))
    implementation(libs.junitJupiterParams)
    implementation(libs.junitJupiterApi)
    implementation(libs.junitJupiterEngine)

    testImplementation(project(":assertions-hamcrest"))
    testImplementation(project(":assertions-kotest"))
    testImplementation(project(":framework-junit"))
}
