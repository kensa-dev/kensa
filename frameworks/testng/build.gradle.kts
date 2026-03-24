description = "Kensa for TestNG"

dependencies {
    api(project(":core"))
    compileOnly(project(":antlr"))
    implementation(libs.antlrRuntime)
    implementation(libs.kotlinReflect)
    implementation(libs.testng)
}
