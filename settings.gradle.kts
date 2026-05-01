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

include("adoptabot")
project(":adoptabot").projectDir = file("examples/adoptabot")

include("docker-logs")
project(":docker-logs").projectDir = file("docker-logs")

include("bom")