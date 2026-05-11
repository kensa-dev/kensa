description = "Kensa Spring Boot Starter — Web (HTTP capture)"

plugins.withId("maven-publish") {
    extensions.configure<PublishingExtension> {
        publications.named<MavenPublication>("mavenJava") {
            artifactId = "kensa-spring-boot-starter-web"
        }
    }
}

dependencies {
    api(project(":integration-spring-boot-starter"))

    compileOnly(libs.springWeb)
    compileOnly(libs.springWebMvc)
    compileOnly(libs.springWebFlux)
    compileOnly(libs.jakartaServletApi)

    testImplementation(libs.springBootStarterTest)
    testImplementation(libs.springBootStarterWeb)
    testImplementation(libs.springBootStarterWebflux)
    testImplementation(platform(libs.kotestBom))
    testImplementation(libs.kotestAssertionsCoreJvm)
    testImplementation(libs.kotestAssertionsJson)
}
