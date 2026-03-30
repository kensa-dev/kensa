import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply(plugin = "com.gradleup.shadow")

description = "A BDD testing framework for Kotlin & Java"

sourceSets {
    create("hooks", Action {
        java.srcDir("src/hooks/java")
        kotlin.srcDir("src/hooks/kotlin")

        compileClasspath += sourceSets["main"].output
    })

    create("example", Action {
        java.srcDir("src/example/java")
        kotlin.srcDir("src/example/kotlin")

        compileClasspath += sourceSets["main"].output
        runtimeClasspath += sourceSets["main"].output
    })

    create("agent", Action {
        kotlin.srcDir("src/agent/kotlin")

        compileClasspath += sourceSets["main"].output
    })

    create("testSupport", Action {
        kotlin.srcDir("src/testSupport/kotlin")

        compileClasspath += sourceSets["test"].output
    })
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("hooks") {
    usingSourceSet(sourceSets.getByName("hooks"))
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("agent") {
    usingSourceSet(sourceSets.getByName("agent"))
}

extensions.getByType(JavaPluginExtension::class.java).registerFeature("testSupport") {
    usingSourceSet(sourceSets.getByName("testSupport"))
    capability("dev.kensa", "core-test-support", "$version")
}

// Private configuration used solely to resolve the plantuml JAR for bundling into
// the shadow JAR. Kept separate from compileOnly so the shadow task has an explicit
// list of JARs to merge (rather than the full runtimeClasspath).
val plantUmlBundle: Configuration by configurations.creating {
    isCanBeConsumed = false
    isCanBeResolved = true
    isVisible = false
}

dependencies {
    compileOnly(project(":antlr"))
    implementation(libs.antlrRuntime)
    implementation(libs.kotlinReflect)
    implementation(libs.minimalJson)
    compileOnly(libs.plantuml)
    plantUmlBundle(libs.plantuml)
    implementation(libs.pebble)
    "agentImplementation"(libs.byteBuddyCore)

    testImplementation(platform(libs.junit6Bom))
    testImplementation(project(":antlr"))
    testImplementation(libs.junit6PlatformLauncher)
    testImplementation(libs.junit6JupiterApi)
    testImplementation(libs.junit6JupiterEngine)
    testImplementation(libs.junit6JupiterParams)

    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.hamcrestCore)
    testImplementation(libs.mockitoKotlin)
    testImplementation(sourceSets["example"].output)

    "testSupportImplementation"(project(":core"))
}

afterEvaluate {
    val shadowJarTask = tasks.named<ShadowJar>("shadowJar")
    listOf("apiElements", "runtimeElements").forEach { config ->
        configurations.named(config) {
            outgoing.artifacts.clear()
            outgoing.artifact(shadowJarTask)
        }
    }
}

tasks {
    named("agentJar", Jar::class.java) {
        archiveBaseName.set(rootProject.name)
        manifest {
            attributes(
                mapOf(
                    "Premain-Class" to "dev.kensa.agent.KensaAgent",
                    "Agent-Class" to "dev.kensa.agent.KensaAgent",
                    "Boot-Class-Path" to "",
                    "Can-Redefine-Classes" to "true",
                    "Can-Retransform-Classes" to "true",
                    "Can-Set-Native-Method-Prefix" to "true",
                    "Implementation-Title" to "Kensa Agent",
                    "Implementation-Version" to rootProject.version
                )
            )
        }
    }

    named<KotlinCompile>("compileExampleKotlin") {
        compilerOptions {
            freeCompilerArgs.addAll("-Xcontext-parameters")
        }
    }

    named<ShadowJar>("shadowJar") {
        archiveClassifier.set("")
        manifest {
            attributes(mapOf("Implementation-Version" to rootProject.version))
        }
        relocate("net.sourceforge.plantuml", "dev.kensa.internal.plantuml")
        relocate("net.atmp", "dev.kensa.internal.atmp")
        relocate("com.plantuml", "dev.kensa.internal.com.plantuml")
        dependsOn(":ui:viteBuild", ":ui2:viteBuild")
        from(project(":antlr").sourceSets["main"].output)
        from(project(":ui").layout.buildDirectory.dir("js").get()) {
            into("/")
        }
        from(project(":ui2").layout.buildDirectory.dir("js").get()) {
            into("/")
        }
        configurations = listOf(plantUmlBundle)
        exclude(
            "org/eclipse/elk/**",
            "org/eclipse/emf/**",
            "org/scilab/**",
            "com/google/common/**",
            "com/google/thirdparty/**",
            "com/google/errorprone/**",
            "com/google/j2objc/**",
            "org/checkerframework/**",
            "org/jspecify/**",
            "META-INF/INDEX.LIST",
            "META-INF/*.SF",
            "META-INF/*.DSA",
            "META-INF/*.RSA",

            "**/*elk*/**",
            "**/*emf*/**",
            "**/*jlatexmath*/**",
            "**/*scilab*/**",
            "**/*guava*/**",
            "**/*common*/**",
            "**/*thirdparty*/**",
            "**/*errorprone*/**",
            "**/*j2objc*/**",
            "**/*checkerframework*/**",
            "**/*jspecify*/**",
            "**/zxing/**",

            "docs/**",
            "gen/**",
            "jcckit/**",
            "stdlib/**",
            "smetana/**",
            "schema/**",
            "svg/**",
            "themes/**",
            "sprites/**",
            "org/stathissideris//**",

            "META-INF/maven/**",
            "META-INF/versions/**",
            "META-INF/proguard/**",
            "META-INF/services/org.eclipse.elk*"
        )
        mergeServiceFiles()
    }

    jar { enabled = false }
}