package dev.kensa.fixture

import dev.kensa.Scenario

class MyScenarioHolder(@field:Scenario val scenario: MyScenario)
class MyScenario(val stringValue: String = "aStringValue")