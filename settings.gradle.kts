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

include("bom")