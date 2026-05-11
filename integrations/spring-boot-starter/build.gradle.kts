description = "Kensa Spring Boot Starter"

plugins.withId("maven-publish") {
    extensions.configure<PublishingExtension> {
        publications.named<MavenPublication>("mavenJava") {
            artifactId = "kensa-spring-boot-starter"
        }
    }
}

dependencies {
    api(project(":core"))
    api(project(":framework-junit5"))

    api(platform(libs.junit5Bom))
    api(libs.junit5JupiterApi)

    api(platform("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}"))
    api(libs.springBootAutoconfigure)
    api(libs.springBootTest)
    api(libs.springTest)

    testImplementation(libs.springBootStarterTest)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
}
