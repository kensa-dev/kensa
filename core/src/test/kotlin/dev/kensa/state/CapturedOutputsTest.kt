package dev.kensa.state

import dev.kensa.outputs.CapturedOutputs
import dev.kensa.outputs.capturedOutput
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class CapturedOutputsTest {

    @Test
    fun `can put with String key`() {
        val capturedOutputs = CapturedOutputs()

        capturedOutputs.put("key", "value")

        capturedOutputs.get<String>("key") shouldBe "value"
    }

    @Test
    fun `can put with typed key`() {
        val capturedOutputs = CapturedOutputs()

        capturedOutputs.put(MyStringOutput, "value")
        capturedOutputs.put(MyIntegerOutput, 10)

        capturedOutputs[MyStringOutput] shouldBe "value"
        capturedOutputs.get<String>("MyStringOutput") shouldBe "value"

        capturedOutputs[MyIntegerOutput] shouldBe 10
        capturedOutputs.getOrNull<Int>("MyIntegerOutput") shouldBe 10
    }

    @Test
    fun `can test whether contains key`() {
        val capturedOutputs = CapturedOutputs()

        capturedOutputs[MyStringOutput] = "value"

        capturedOutputs.contains(MyStringOutput) shouldBe true
        capturedOutputs.contains(MyIntegerOutput) shouldBe false

        capturedOutputs.contains("SomeOtherKey") shouldBe false
        capturedOutputs.contains(MyStringOutput.key) shouldBe true
    }

    @Test
    fun `throws on get when no value`() {
        val capturedOutputs = CapturedOutputs()

        shouldThrow<NoSuchElementException> {
            capturedOutputs[MyStringOutput]
        }
    }

    @Test
    fun `throws when result not expected type`() {
        val capturedOutputs = CapturedOutputs()

        capturedOutputs.put("MyStringOutput", 10)

        shouldThrow<IllegalArgumentException> {
            capturedOutputs[MyStringOutput]
        }
        shouldThrow<IllegalArgumentException> {
            capturedOutputs.getOrNull(MyStringOutput)
        }
    }

    private val MyStringOutput = capturedOutput<String>("MyStringOutput")
    private val MyIntegerOutput = capturedOutput<Int>("MyIntegerOutput")
}
