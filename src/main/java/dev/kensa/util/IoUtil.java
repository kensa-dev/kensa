package dev.kensa.util;

import dev.kensa.KensaException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;

public final class IoUtil {

    private static final DeleteAllVisitor DELETE_ALL_VISITOR = new DeleteAllVisitor();

    public static void recreate(Path path) {
        try {
            if (Files.exists(path)) {
                Files.walkFileTree(path, DELETE_ALL_VISITOR);
            }
            Files.createDirectories(path);
        } catch (IOException e) {
            throw new KensaException(String.format("Unable to recreate directory [%s]", path), e);
        }
    }

    public static void copyResource(String url, Path destDir) {
        try {
            InputStream resourceStream = IoUtil.class.getResourceAsStream(url);
            if (resourceStream == null) {
                throw new KensaException(String.format("Did not find resource [%s]", url));
            }
            if (!Files.isDirectory(destDir)) {
                throw new KensaException(String.format("Destination [%s] is not a directory", destDir.toString()));
            }
            Files.copy(resourceStream, destDir.resolve(Paths.get(url).getFileName()));
        } catch (IOException e) {
            throw new KensaException(String.format("Unable to copy resource [%s] to [%s]", url, destDir), e);
        }
    }

    private static class DeleteAllVisitor extends SimpleFileVisitor<Path> {

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
            Files.delete(file);
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path directory, IOException exception) throws IOException {
            Files.delete(directory);
            return CONTINUE;
        }
    }
}
