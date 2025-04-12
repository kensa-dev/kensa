rootProject.name = "kensa"

include("core")

include("ui")

include("assertions-assertj")
project(":assertions-assertj").projectDir = file("assertions/assertj")

include("assertions-hamcrest")
project(":assertions-hamcrest").projectDir = file("assertions/hamcrest")

include("assertions-hamkrest")
project(":assertions-hamkrest").projectDir = file("assertions/hamkrest")

include("assertions-kotest")
project(":assertions-kotest").projectDir = file("assertions/kotest")

include("framework-junit")
project(":framework-junit").projectDir = file("frameworks/junit")

include("framework-kotest")
project(":framework-kotest").projectDir = file("frameworks/kotest")

include("framework-testng")
project(":framework-testng").projectDir = file("frameworks/testng")

