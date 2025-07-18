import java.nio.file.Paths

description = "Kensa for JUnit5"

private val uiBuildDir = Paths.get(project(":ui").layout.buildDirectory.get().toString(), "js")

sourceSets {
    create("example") {
        java.srcDir("src/example/java")
        kotlin.srcDir("src/example/kotlin")

        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    }
}

dependencies {
    api(project(":core"))
    implementation(libs.kotlinReflect)

    implementation(platform(libs.junitBom))
    implementation(libs.junitJupiterParams)
    implementation(libs.junitJupiterApi)
    implementation(libs.junitJupiterEngine)
    // TODO - Remove when package stuff not reliant on JUnit
    implementation(libs.junitPlatformLauncher)

    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.kotestAssertionsJson)
    testImplementation(libs.hamkrest)
    testImplementation(sourceSets["example"].output)
    testImplementation(platform(libs.junitBom))
    testImplementation(libs.junitPlatformTestKit)
    testImplementation(libs.junitPlatformEngine)
    testImplementation(libs.jsoup)
    testImplementation(libs.minimalJson)

    testRuntimeOnly(sourceSets["example"].output)
    testRuntimeOnly(sourceSets["example"].resources)
    testRuntimeOnly(project(":assertions-hamcrest"))
    testRuntimeOnly(project(":assertions-hamkrest"))

    "exampleImplementation"(project(":assertions-hamcrest"))
    "exampleImplementation"(project(":assertions-hamkrest"))
    "exampleImplementation"(project(":core"))
    "exampleImplementation"(platform(libs.junitBom))
    "exampleImplementation"(libs.junitJupiterApi)
    "exampleImplementation"(libs.junitJupiterParams)

    "exampleRuntimeOnly"(files(uiBuildDir))
    "exampleRuntimeOnly"(project(":core")) {
        capabilities {
            requireCapability("dev.kensa:core-agent")
        }
    }
}

tasks.named<Test>("test") {
    dependsOn(":core:agentJar")

    jvmArgs("""-javaagent:${project(":core").tasks.named<Jar>("agentJar").get().archiveFile.get().asFile.absolutePath}""")
}
