rootProject.name = "kensa"

include("antlr")

include("core")

include("ui")

include("compiler-plugin")
project(":compiler-plugin").projectDir = file("compiler/compiler-plugin")

include("assertions-assertj")
project(":assertions-assertj").projectDir = file("assertions/assertj")

include("assertions-hamcrest")
project(":assertions-hamcrest").projectDir = file("assertions/hamcrest")

include("assertions-hamkrest")
project(":assertions-hamkrest").projectDir = file("assertions/hamkrest")

include("assertions-kotest")
project(":assertions-kotest").projectDir = file("assertions/kotest")

include("kotest-test-support")
project(":kotest-test-support").projectDir = file("test-support/kotest/core")

include("kotest-test-support-xml")
project(":kotest-test-support-xml").projectDir = file("test-support/kotest/xml")

include("kotest-test-support-json")
project(":kotest-test-support-json").projectDir = file("test-support/kotest/json")

include("hamkrest-test-support")
project(":hamkrest-test-support").projectDir = file("test-support/hamkrest/core")

include("hamkrest-test-support-xml")
project(":hamkrest-test-support-xml").projectDir = file("test-support/hamkrest/xml")

include("hamkrest-test-support-json")
project(":hamkrest-test-support-json").projectDir = file("test-support/hamkrest/json")

include("framework-junit5")
project(":framework-junit5").projectDir = file("frameworks/junit/junit5")

include("framework-junit6")
project(":framework-junit6").projectDir = file("frameworks/junit/junit6")

include("framework-kotest")
project(":framework-kotest").projectDir = file("frameworks/kotest")

include("framework-testng")
project(":framework-testng").projectDir = file("frameworks/testng")

include("framework-uitesting")
project(":framework-uitesting").projectDir = file("frameworks/uitesting")

include("framework-uitesting-junit5")
project(":framework-uitesting-junit5").projectDir = file("frameworks/uitesting/junit5")

include("framework-uitesting-junit6")
project(":framework-uitesting-junit6").projectDir = file("frameworks/uitesting/junit6")

include("framework-playwright")
project(":framework-playwright").projectDir = file("frameworks/playwright")

include("framework-playwright-junit5")
project(":framework-playwright-junit5").projectDir = file("frameworks/playwright/junit5")

include("framework-playwright-junit6")
project(":framework-playwright-junit6").projectDir = file("frameworks/playwright/junit6")

include("framework-selenium")
project(":framework-selenium").projectDir = file("frameworks/selenium")

include("framework-selenium-junit5")
project(":framework-selenium-junit5").projectDir = file("frameworks/selenium/junit5")

include("framework-selenium-junit6")
project(":framework-selenium-junit6").projectDir = file("frameworks/selenium/junit6")

include("adoptabot")
project(":adoptabot").projectDir = file("examples/adoptabot")

include("docker-logs")
project(":docker-logs").projectDir = file("docker-logs")

include("integration-spring-boot-starter")
project(":integration-spring-boot-starter").projectDir = file("integrations/spring-boot-starter")

include("integration-spring-boot-starter-web")
project(":integration-spring-boot-starter-web").projectDir = file("integrations/spring-boot-starter-web")

include("bom")
include("doc-snippets")
project(":doc-snippets").projectDir = file("kensa.dev/snippets")
