plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(kotlin("gradle-plugin", version="1.6.10"))
}