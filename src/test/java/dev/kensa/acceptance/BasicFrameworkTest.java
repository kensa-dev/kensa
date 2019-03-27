package dev.kensa.acceptance;

import dev.kensa.acceptance.example.TestWithMultiplePassingTests;
import dev.kensa.acceptance.example.TestWithSinglePassingTest;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static dev.kensa.acceptance.KensaTestExecutor.executeTests;
import static org.assertj.core.api.Assertions.assertThat;

class BasicFrameworkTest extends KensaAcceptanceTest {

    @Test
    void createsOutputFilesWhenMultipleTestClassesExecuted() {
        Class[] testClasses = {TestWithSinglePassingTest.class, TestWithMultiplePassingTests.class};
        executeTests(testClasses);

        for (Class testClass : testClasses) {
            Path outputFilePath = kensaOutputDir.resolve(testClass.getName() + ".html");
            assertThat(Files.exists(outputFilePath))
                    .withFailMessage("Expected file [%s] to exist.", outputFilePath)
                    .isTrue();
        }

        assertThat(Files.exists(kensaOutputDir.resolve("index.html"))).isTrue();
        assertThat(Files.exists(kensaOutputDir.resolve("kensa.js"))).isTrue();
    }

//    @Test
//    void executesWhenAllTestsAreDisabled() {
//        executeAllTestsIn(TestWithAllTestsDisabled.class);
//    }
//
//    @Test
//    void testExecutionContextContainsCorrectValues_SinglePassingTest() {
//        executeAllTestsIn(TestWithSinglePassingTest.class);
//    }
//
//    @Test
//    void testExecutionContextContainsCorrectValues_SingleFailingTest() {
//        executeAllTestsIn(TestWithSingleFailingTest.class);
//    }
//
//    @Test
//    void testExecutionContextContainsCorrectValues_ParallelPassingTests() {
//        executeAllTestsInParallelIn(TestWithMultiplePassingTests.class);
//    }

}
