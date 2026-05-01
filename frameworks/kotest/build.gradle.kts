description = "Kensa for Kotest"

dependencies {
    api(project(":core"))
    compileOnly(project(":antlr"))
    implementation(libs.antlr)
    implementation(libs.kotlinReflect)

    compileOnly(platform(libs.kotestBom))
    compileOnly(libs.kotestFrameworkEngine)
    compileOnly(libs.kotestRunnerJunit5)
}


tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
