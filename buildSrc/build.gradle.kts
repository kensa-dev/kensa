plugins {
    `java-gradle-plugin`
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
}

repositories {
	mavenCentral()
}

dependencies {
	implementation(kotlin("gradle-plugin", version="1.5.30"))
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}