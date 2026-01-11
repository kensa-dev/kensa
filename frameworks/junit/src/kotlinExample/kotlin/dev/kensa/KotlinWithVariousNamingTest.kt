package dev.kensa

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class KotlinWithVariousNamingTest : KotlinExampleTest() {

    @DisplayName("My Named Test 1")
    @Test
    fun myTest1() {
    }

    @Test
    fun test1() {
    }

    @Test
    fun `test 1`() {
    }

    @Test
    fun `Test 1`() {
    }

    @Test
    fun `Test 1 🤡`() {
    }

    @ParameterizedTest
    @ValueSource(strings = ["one", "two", "three 3️⃣"])
    fun `Test 1 🤡😱`(value: String) {
    }
}