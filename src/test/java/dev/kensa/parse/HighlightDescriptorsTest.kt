package dev.kensa.parse

import dev.kensa.MaxHighlightColoursBehaviour.RollOver
import dev.kensa.MaxHighlightColoursBehaviour.Stop
import dev.kensa.util.NamedValue
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class HighlightDescriptorsTest {
    
    @Test
    fun `returns null when value not found and no identifier given`() {
        val underTest = HighlightDescriptors.from(listOf(NamedValue("field1", "Value of field1")), 10, RollOver)
        
        underTest.highlightDescriptorFor("unknown value").shouldBeNull()
    }
    
    @Test
    fun `returns null when value and identifier not found`() {
        val underTest = HighlightDescriptors.from(listOf(NamedValue("field1", "Value of field1")), 10, RollOver)
        
        underTest.highlightDescriptorFor("unknown value", "unknown identifier").shouldBeNull()
    }
    
    @Test
    fun `matches on identifier first`() {
        val underTest = HighlightDescriptors.from(listOf(
            NamedValue("field1", "Shared value"),
            NamedValue("field2", "Shared value"),
        ), 10, RollOver)
        
        underTest.highlightDescriptorFor("Doesn't matter", "field2") shouldBe HighlightDescriptor("Shared value", "field2", "1")
    }
    
    @Test
    fun `matches on unambiguous value`() {
        val underTest = HighlightDescriptors.from(listOf(
            NamedValue("field1", "Value of field1"),
            NamedValue("field2", "Value of field2"),
        ), 10, RollOver)
        
        underTest.highlightDescriptorFor("Value of field2") shouldBe HighlightDescriptor("Value of field2", "field2", "1")
    }
    
    @Test
    fun `returns ambiguous descriptor for ambiguous value`() {
        val underTest = HighlightDescriptors.from(listOf(
            NamedValue("field1", "Shared value"),
            NamedValue("field2", "Shared value"),
        ), 10, RollOver)
        
        underTest.highlightDescriptorFor("Shared value") shouldBe HighlightDescriptor("Shared value", "'field1' or 'field2' (ambiguous)", "ambiguous")
    }
    
    @Test
    fun `returns ambiguous descriptor for ambiguous value and unknown identifier`() {
        val underTest = HighlightDescriptors.from(listOf(
            NamedValue("field1", "Shared value"),
            NamedValue("field2", "Shared value"),
        ), 10, RollOver)
        
        underTest.highlightDescriptorFor("Shared value", "unknownfield") shouldBe HighlightDescriptor("Shared value", "'field1' or 'field2' (ambiguous)", "ambiguous")
    }
    
    @Test
    fun `final descriptor list does not contain ambiguous descriptors`() {
        val underTest = HighlightDescriptors.from(listOf(
            NamedValue("field1", "Shared value"),
            NamedValue("field2", "Shared value"),
            NamedValue("field3", "Unique value"),
        ), 10, RollOver)
        
        underTest.descriptors shouldContainExactlyInAnyOrder(
                listOf(
                    HighlightDescriptor("Shared value", "field1", "1"),
                    HighlightDescriptor("Shared value", "field2", "2"),
                    HighlightDescriptor("Unique value", "field3", "3"),
                ))
    }
    
    @Test
    fun `final descriptor list should have colour index assigned by search order first`() {
        val underTest = HighlightDescriptors.from(listOf(
            NamedValue("field1", "Shared value 1"),
            NamedValue("field2", "Shared value 1"),
            NamedValue("field3", "Value of field3"),
            NamedValue("field4", "Value of field4"),
            NamedValue("field5", "Value of field5"),
            NamedValue("field6", "Shared value 2"),
            NamedValue("field7", "Shared value 2"),
            NamedValue("field8", "Value of field8"),
            NamedValue("field9", "Value of field9"),
            NamedValue("field10", "Value of field10"),
        ), 10, RollOver)
        
        underTest.highlightDescriptorFor("Doesn't matter", "field4") shouldBe HighlightDescriptor("Value of field4", "field4", "1") 
        underTest.highlightDescriptorFor("Doesn't matter", "field3") shouldBe HighlightDescriptor("Value of field3", "field3", "2") 
        underTest.highlightDescriptorFor("Doesn't matter", "field1") shouldBe HighlightDescriptor("Shared value 1", "field1", "3")
        
        underTest.highlightDescriptorFor("Value of field3") shouldBe HighlightDescriptor("Value of field3", "field3", "2")
        underTest.highlightDescriptorFor("Value of field8") shouldBe HighlightDescriptor("Value of field8", "field8", "4")
        underTest.highlightDescriptorFor("Shared value 2") shouldBe HighlightDescriptor("Shared value 2", "'field6' or 'field7' (ambiguous)", "ambiguous")
        
        underTest.highlightDescriptorFor("Value of field8") shouldBe HighlightDescriptor("Value of field8", "field8", "4")
        
        underTest.descriptors shouldContainExactlyInAnyOrder(
                listOf(
                    // The first four are assigned an index based on when they were requested by the highlightDescriptorFor() method
                    HighlightDescriptor("Value of field4", "field4", "1"),
                    HighlightDescriptor("Value of field3", "field3", "2"),
                    HighlightDescriptor("Shared value 1", "field1", "3"),
                    HighlightDescriptor("Value of field8", "field8", "4"),
                    
                    // The remainder are just assigned according to their order in the original NamedValue list
                    HighlightDescriptor("Shared value 1", "field2", "5"),
                    HighlightDescriptor("Value of field5", "field5", "6"),
                    HighlightDescriptor("Shared value 2", "field6", "7"),
                    HighlightDescriptor("Shared value 2", "field7", "8"),
                    HighlightDescriptor("Value of field9", "field9", "9"),
                    HighlightDescriptor("Value of field10", "field10", "10"),
                ))
    }

    @Test
    fun `colour indexes can over when there are too many`() {
        val underTest = HighlightDescriptors.from(listOf(
            NamedValue("field1", "Shared value 1"),
            NamedValue("field2", "Shared value 1"),
            NamedValue("field3", "Value of field3"),
            NamedValue("field4", "Value of field4"),
            NamedValue("field5", "Value of field5"),
            NamedValue("field6", "Shared value 2"),
            NamedValue("field7", "Shared value 2"),
            NamedValue("field8", "Value of field8"),
            NamedValue("field9", "Value of field9"),
            NamedValue("field10", "Value of field10"),
        ), 3, RollOver)

        underTest.highlightDescriptorFor("Doesn't matter", "field4") shouldBe HighlightDescriptor("Value of field4", "field4", "1")
        underTest.highlightDescriptorFor("Doesn't matter", "field3") shouldBe HighlightDescriptor("Value of field3", "field3", "2")
        underTest.highlightDescriptorFor("Doesn't matter", "field1") shouldBe HighlightDescriptor("Shared value 1", "field1", "3")

        underTest.highlightDescriptorFor("Value of field3") shouldBe HighlightDescriptor("Value of field3", "field3", "2")
        underTest.highlightDescriptorFor("Value of field8") shouldBe HighlightDescriptor("Value of field8", "field8", "1")
        underTest.highlightDescriptorFor("Shared value 2") shouldBe HighlightDescriptor("Shared value 2", "'field6' or 'field7' (ambiguous)", "ambiguous")

        underTest.highlightDescriptorFor("Value of field8") shouldBe HighlightDescriptor("Value of field8", "field8", "1")

        underTest.descriptors shouldContainExactlyInAnyOrder(
                listOf(
                    // The first four are assigned an index based on when they were requested by the highlightDescriptorFor() method
                    HighlightDescriptor("Value of field4", "field4", "1"),
                    HighlightDescriptor("Value of field3", "field3", "2"),
                    HighlightDescriptor("Shared value 1", "field1", "3"),
                    HighlightDescriptor("Value of field8", "field8", "1"),

                    // The remainder are just assigned according to their order in the original NamedValue list
                    HighlightDescriptor("Shared value 1", "field2", "2"),
                    HighlightDescriptor("Value of field5", "field5", "3"),
                    HighlightDescriptor("Shared value 2", "field6", "1"),
                    HighlightDescriptor("Shared value 2", "field7", "2"),
                    HighlightDescriptor("Value of field9", "field9", "3"),
                    HighlightDescriptor("Value of field10", "field10", "1"),
                ))
    }

    @Test
    fun `colour indexes stop when there are too many`() {
        val underTest = HighlightDescriptors.from(listOf(
            NamedValue("field1", "Shared value 1"),
            NamedValue("field2", "Shared value 1"),
            NamedValue("field3", "Value of field3"),
            NamedValue("field4", "Value of field4"),
            NamedValue("field5", "Value of field5"),
            NamedValue("field6", "Shared value 2"),
            NamedValue("field7", "Shared value 2"),
            NamedValue("field8", "Value of field8"),
            NamedValue("field9", "Value of field9"),
            NamedValue("field10", "Value of field10"),
        ), 3, Stop)

        underTest.highlightDescriptorFor("Doesn't matter", "field4") shouldBe HighlightDescriptor("Value of field4", "field4", "1")
        underTest.highlightDescriptorFor("Doesn't matter", "field3") shouldBe HighlightDescriptor("Value of field3", "field3", "2")
        underTest.highlightDescriptorFor("Doesn't matter", "field1") shouldBe HighlightDescriptor("Shared value 1", "field1", "3")

        underTest.highlightDescriptorFor("Value of field3") shouldBe HighlightDescriptor("Value of field3", "field3", "2")
        underTest.highlightDescriptorFor("Value of field8") shouldBe HighlightDescriptor("Value of field8", "field8", "none")
        underTest.highlightDescriptorFor("Shared value 2") shouldBe HighlightDescriptor("Shared value 2", "'field6' or 'field7' (ambiguous)", "ambiguous")

        underTest.highlightDescriptorFor("Value of field8") shouldBe HighlightDescriptor("Value of field8", "field8", "none")

        underTest.descriptors shouldContainExactlyInAnyOrder(
                listOf(
                    // The first four are assigned an index based on when they were requested by the highlightDescriptorFor() method
                    HighlightDescriptor("Value of field4", "field4", "1"),
                    HighlightDescriptor("Value of field3", "field3", "2"),
                    HighlightDescriptor("Shared value 1", "field1", "3"),
                    HighlightDescriptor("Value of field8", "field8", "none"),

                    // The remainder are just assigned according to their order in the original NamedValue list
                    HighlightDescriptor("Shared value 1", "field2", "none"),
                    HighlightDescriptor("Value of field5", "field5", "none"),
                    HighlightDescriptor("Shared value 2", "field6", "none"),
                    HighlightDescriptor("Shared value 2", "field7", "none"),
                    HighlightDescriptor("Value of field9", "field9", "none"),
                    HighlightDescriptor("Value of field10", "field10", "none"),
                ))
    }
}