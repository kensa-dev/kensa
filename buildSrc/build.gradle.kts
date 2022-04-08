import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin", version = "1.6.20"))
}

tasks {

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions {
            jvmTarget = "11"
        }
    }

    named<JavaCompile>("compileJava") {
        sourceCompatibility = JavaVersion.VERSION_11.majorVersion
        targetCompatibility = JavaVersion.VERSION_11.majorVersion
    }
}