description = "Hamkrest assertion helpers for Kensa"

dependencies {
    implementation(project(":core"))

    api(libs.hamkrest)
    implementation(libs.awaitilityKotlin)
}