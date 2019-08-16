plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(kotlin("gradle-plugin"))
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}