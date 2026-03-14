description = "Hamcrest assertion helpers for Kensa"

dependencies {
    implementation(project(":core"))

    api(libs.hamcrestCore)
    api(libs.awaitilityKotlin)
}