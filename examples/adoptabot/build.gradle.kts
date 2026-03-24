dependencies {
    implementation(libs.http4kCore)
    implementation(libs.http4kJackson)
    testImplementation(libs.http4kKotest)

    testImplementation(libs.http4kClientOkHttp)
    testImplementation(libs.jacksonKotlin)

    testImplementation(platform(libs.junit6Bom))
    testImplementation(libs.junit6JupiterParams)
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)

    testImplementation(project(":assertions-hamcrest"))
    testImplementation(project(":assertions-kotest"))
    testImplementation(project(":framework-junit6"))
}
