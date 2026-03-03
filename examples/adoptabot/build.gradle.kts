dependencies {
    implementation(libs.http4kCore)
    implementation(libs.http4kJackson)
    implementation(libs.http4kKotest)

    testImplementation(libs.http4kClientOkHttp)
    testImplementation(libs.jacksonKotlin)

    implementation(platform(libs.junit6Bom))
    implementation(libs.junit6JupiterParams)
    implementation(libs.junit6JupiterApi)
    implementation(libs.junit6JupiterEngine)

    testImplementation(project(":assertions-hamcrest"))
    testImplementation(project(":assertions-kotest"))
    testImplementation(project(":framework-junit6"))
}
