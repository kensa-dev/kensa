import java.nio.file.Paths

description = "Kensa for Kotest"

private val uiBuildDir = Paths.get(project(":ui").layout.buildDirectory.get().toString(), "js")

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
