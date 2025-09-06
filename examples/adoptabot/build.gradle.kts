dependencies {
    implementation(libs.http4kCore)
    implementation(libs.http4kJackson)
    implementation(libs.http4kKotest)

    testImplementation(libs.http4kClientOkHttp)
    testImplementation(libs.jacksonKotlin)

    implementation(platform(libs.junitBom))
    implementation(libs.junitJupiterParams)
    implementation(libs.junitJupiterApi)
    implementation(libs.junitJupiterEngine)

    testImplementation(project(":assertions-hamcrest"))
    testImplementation(project(":assertions-kotest"))
    testImplementation(project(":framework-junit"))
}
