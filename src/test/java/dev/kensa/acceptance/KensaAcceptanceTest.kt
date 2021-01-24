package dev.kensa.acceptance;

import dev.kensa.Kensa;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static dev.kensa.output.OutputStyle.MultiFile;

abstract class KensaAcceptanceTest {

    static Path kensaOutputDir;

    @BeforeAll
    static void beforeAll(@TempDir Path tempDir) {
        kensaOutputDir = tempDir.resolve("kensa-output");

        Kensa.configure()
             .withOutputDir(kensaOutputDir)
             .withOutputStyle(MultiFile)
        ;
    }
}
