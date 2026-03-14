description = "AssertJ assertion helpers for Kensa"

dependencies {
    implementation(project(":core"))

    api(libs.assertJCore)
    api(libs.awaitilityKotlin)
}