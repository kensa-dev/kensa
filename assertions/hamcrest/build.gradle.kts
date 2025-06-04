description = "Hamcrest assertion helpers for Kensa"

dependencies {
    implementation(project(":core"))

    api(libs.hamcrestCore)
    implementation(libs.awaitilityKotlin)
}