package dev.kensa.parse

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class RegexPatternsTest {

    @Nested
    inner class ChainedCallPattern {

        @Test
        fun `matches property access chain`() {
            val result = RegexPatterns.chainedCallPattern.matchEntire("target.a.b.c").shouldNotBeNull()
            result.groupValues[1] shouldBe "target"
            result.groupValues[2] shouldBe ""
            result.groupValues[3] shouldBe "a.b.c"
        }

        @Test
        fun `matches parameterless calls and property accesses`() {
            val result = RegexPatterns.chainedCallPattern.matchEntire("target.foo().foo.foo.a.b").shouldNotBeNull()
            result.groupValues[1] shouldBe "target"
            result.groupValues[2] shouldBe ""
            result.groupValues[3] shouldBe "foo().foo.foo.a.b"
        }

        @Test
        fun `matches mixed parameterless calls`() {
            val result = RegexPatterns.chainedCallPattern.matchEntire("thing.m1().b.m2()").shouldNotBeNull()
            result.groupValues[1] shouldBe "thing"
            result.groupValues[2] shouldBe ""
            result.groupValues[3] shouldBe "m1().b.m2()"
        }

        @Test
        fun `matches standalone identifier with no chain`() {
            val result = RegexPatterns.chainedCallPattern.matchEntire("target").shouldNotBeNull()
            result.groupValues[1] shouldBe "target"
            result.groupValues[2] shouldBe ""
            result.groupValues[3] shouldBe ""
        }

        @Test
        fun `matches identifier with parens followed by chain`() {
            val result = RegexPatterns.chainedCallPattern.matchEntire("myScenarioFun().value").shouldNotBeNull()
            result.groupValues[1] shouldBe "myScenarioFun"
            result.groupValues[2] shouldBe "()"
            result.groupValues[3] shouldBe "value"
        }

        @Test
        fun `matches identifier with parens and no chain`() {
            val result = RegexPatterns.chainedCallPattern.matchEntire("myScenarioFun()").shouldNotBeNull()
            result.groupValues[1] shouldBe "myScenarioFun"
            result.groupValues[2] shouldBe "()"
            result.groupValues[3] shouldBe ""
        }

        @Test
        fun `does not match call with single argument`() {
            RegexPatterns.chainedCallPattern.matchEntire("thing.foo(a)").shouldBeNull()
        }

        @Test
        fun `does not match call with multiple arguments`() {
            RegexPatterns.chainedCallPattern.matchEntire("target.foo(a, b).c").shouldBeNull()
        }
    }

    @Nested
    inner class FixturesPattern {

        @Test
        fun `matches square bracket syntax`() {
            val result = RegexPatterns.fixturesPattern.matchEntire("fixtures[MyFixture]").shouldNotBeNull()
            result.groupValues[1] shouldBe "MyFixture"
            result.groupValues[2] shouldBe ""
        }

        @Test
        fun `matches round bracket syntax`() {
            val result = RegexPatterns.fixturesPattern.matchEntire("fixtures(MyFixture)").shouldNotBeNull()
            result.groupValues[1] shouldBe "MyFixture"
            result.groupValues[2] shouldBe ""
        }

        @Test
        fun `matches curly bracket syntax`() {
            val result = RegexPatterns.fixturesPattern.matchEntire("fixtures{MyFixture}").shouldNotBeNull()
            result.groupValues[1] shouldBe "MyFixture"
            result.groupValues[2] shouldBe ""
        }

        @Test
        fun `matches with parameterless call and property chain`() {
            val result = RegexPatterns.fixturesPattern.matchEntire("fixtures[MyFixture].foo().bar").shouldNotBeNull()
            result.groupValues[1] shouldBe "MyFixture"
            result.groupValues[2] shouldBe "foo().bar"
        }

        @Test
        fun `matches single-level qualified target`() {
            val result = RegexPatterns.fixturesPattern.matchEntire("fixtures(MoreJavaTestFixtures.BOOLEAN_FIXTURE)").shouldNotBeNull()
            result.groupValues[1] shouldBe "BOOLEAN_FIXTURE"
            result.groupValues[2] shouldBe ""
        }

        @Test
        fun `matches multi-level qualified target`() {
            val result = RegexPatterns.fixturesPattern.matchEntire("fixtures[a.b.Pkg.MyFixture]").shouldNotBeNull()
            result.groupValues[1] shouldBe "MyFixture"
            result.groupValues[2] shouldBe ""
        }

        @Test
        fun `matches qualified target with chain`() {
            val result = RegexPatterns.fixturesPattern.matchEntire("fixtures(MoreJavaTestFixtures.BOOLEAN_FIXTURE).someProperty").shouldNotBeNull()
            result.groupValues[1] shouldBe "BOOLEAN_FIXTURE"
            result.groupValues[2] shouldBe "someProperty"
        }

        @Test
        fun `does not match chain with arguments`() {
            RegexPatterns.fixturesPattern.matchEntire("fixtures[MyFixture].method(arg)").shouldBeNull()
        }
    }

    @Nested
    inner class OutputsByNamePattern {

        @Test
        fun `matches square bracket syntax`() {
            val result = RegexPatterns.outputsByNamePattern.matchEntire("outputs[MyOutput]").shouldNotBeNull()
            result.groupValues[1] shouldBe "MyOutput"
            result.groupValues[2] shouldBe ""
        }

        @Test
        fun `matches round bracket syntax`() {
            val result = RegexPatterns.outputsByNamePattern.matchEntire("outputs(MyOutput)").shouldNotBeNull()
            result.groupValues[1] shouldBe "MyOutput"
            result.groupValues[2] shouldBe ""
        }

        @Test
        fun `matches with property chain`() {
            val result = RegexPatterns.outputsByNamePattern.matchEntire("outputs[MyOutput].foo").shouldNotBeNull()
            result.groupValues[1] shouldBe "MyOutput"
            result.groupValues[2] shouldBe "foo"
        }

        @Test
        fun `does not match curly bracket syntax`() {
            RegexPatterns.outputsByNamePattern.matchEntire("outputs{MyOutput}").shouldBeNull()
        }

        @Test
        fun `matches single-level qualified target`() {
            val result = RegexPatterns.outputsByNamePattern.matchEntire("outputs(MoreKotlinCapturedOutputs.BooleanOutput)").shouldNotBeNull()
            result.groupValues[1] shouldBe "BooleanOutput"
            result.groupValues[2] shouldBe ""
        }

        @Test
        fun `does not match chain with arguments`() {
            RegexPatterns.outputsByNamePattern.matchEntire("outputs[MyOutput].method(arg)").shouldBeNull()
        }
    }

    @Nested
    inner class OutputsByKeyPattern {

        @Test
        fun `matches quoted key`() {
            val result = RegexPatterns.outputsByKeyPattern.matchEntire("""outputs("myKey")""").shouldNotBeNull()
            result.groupValues[1] shouldBe "myKey"
            result.groupValues[2] shouldBe ""
        }

        @Test
        fun `matches with parameterless call and property chain`() {
            val result = RegexPatterns.outputsByKeyPattern.matchEntire("""outputs("myKey").foo().bar""").shouldNotBeNull()
            result.groupValues[1] shouldBe "myKey"
            result.groupValues[2] shouldBe "foo().bar"
        }

        @Test
        fun `does not match unquoted key`() {
            RegexPatterns.outputsByKeyPattern.matchEntire("outputs(myKey)").shouldBeNull()
        }

        @Test
        fun `does not match chain with arguments`() {
            RegexPatterns.outputsByKeyPattern.matchEntire("""outputs("myKey").method(arg)""").shouldBeNull()
        }
    }

    @Nested
    inner class SingleCallWithArgumentsPattern {

        @Test
        fun `matches parameterless call`() {
            val result = RegexPatterns.singleCallWithArgumentsPattern.matchEntire("foo()").shouldNotBeNull()
            result.groups["function"]!!.value shouldBe "foo"
            result.groups["receiver"]?.value shouldBe null
        }

        @Test
        fun `matches call with arguments`() {
            val result = RegexPatterns.singleCallWithArgumentsPattern.matchEntire("foo(a, b)").shouldNotBeNull()
            result.groups["function"]!!.value shouldBe "foo"
        }

        @Test
        fun `matches call with receiver`() {
            val result = RegexPatterns.singleCallWithArgumentsPattern.matchEntire("obj.foo(a)").shouldNotBeNull()
            result.groups["function"]!!.value shouldBe "foo"
            result.groups["receiver"]!!.value shouldBe "obj"
        }

        @Test
        fun `does not match bare identifier`() {
            RegexPatterns.singleCallWithArgumentsPattern.matchEntire("foo").shouldBeNull()
        }

        @Test
        fun `does not match property access chain`() {
            RegexPatterns.singleCallWithArgumentsPattern.matchEntire("foo.bar").shouldBeNull()
        }
    }
}
