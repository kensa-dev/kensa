package dev.kensa.render.diagram.directive

import dev.kensa.render.diagram.directive.UmlTitle.Companion.title
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class UmlTitleTest {
    @Test
    fun `can obtain simple title grammar`() {
        val title = title("Some Title")

        title.asUml() shouldBe listOf("title", "Some Title", "end title")
    }
}