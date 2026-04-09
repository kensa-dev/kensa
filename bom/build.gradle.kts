import org.gradle.api.publish.maven.MavenPublication

plugins {
    `java-platform`
    `maven-publish`
}

dependencies {
    constraints {
        api(project(":core"))
        api(project(":assertions-assertj"))
        api(project(":assertions-hamcrest"))
        api(project(":assertions-hamkrest"))
        api(project(":assertions-kotest"))
        api(project(":framework-junit5"))
        api(project(":framework-junit6"))
        api(project(":framework-kotest"))
        api(project(":framework-testng"))
    }
}

publishing {
    publications {
        create<MavenPublication>("bomPublication") {
            artifactId = "${rootProject.name}-bom"
            from(components["javaPlatform"])
            pom {
                name.set("Kensa BOM")
                description.set("Bill of Materials for Kensa modules")
                url.set("https://kensa.dev")
                licenses {
                    license {
                        name.set("Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.html")
                    }
                }
                developers {
                    developer {
                        name.set("Paul Brooks")
                        email.set("paul@kensa.dev")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:kensa-dev/kensa.git")
                    developerConnection.set("scm:git:git@github.com:kensa-dev/kensa.git")
                    url.set("git@github.com:kensa-dev/kensa.git")
                }
            }
        }
    }
    repositories {
        maven {
            url = uri(rootProject.layout.buildDirectory.dir("staging-deploy").get().asFile.absolutePath)
        }
    }
}