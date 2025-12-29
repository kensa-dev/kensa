import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    antlr
    `java-library`
}

dependencies {
    antlr(libs.antlr)
    api(libs.antlr)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks {

    withType<AntlrTask> {
        arguments = arguments + listOf(
            "-listener",
            "-no-visitor",
            "-Xexact-output-dir"
        )
    }

    withType<JavaCompile> {
        dependsOn("generateGrammarSource")
    }
}