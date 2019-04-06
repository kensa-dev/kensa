package dev.kensa.util;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.lang.System.getProperty;

public class SourceCodeIndex {

    private static final SourceCodeIndex INSTANCE = new SourceCodeIndex();

    public static Path locate(Class<?> clazz) {
        return INSTANCE.doLocate(clazz);
    }

    private final Path workingDirectory = Paths.get(getProperty("user.dir"));

    private SourceCodeIndex() {
    }

    private Path doLocate(Class<?> clazz) {
        String name = clazz.getName();

        int s = name.indexOf("$");
        if(s > 0) {
            name = name.substring(0, s);
        }
        Walker walker = new Walker(name);
        try {
            Files.walkFileTree(workingDirectory, walker);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return walker.result();
    }

    private static class Walker implements FileVisitor<Path> {
        private final PathMatcher pathMatcher;
        private Path result;

        Walker(String name) {
            pathMatcher = FileSystems.getDefault().getPathMatcher(String.format("glob:**/%s.java", name.replaceAll("\\.", "/")));
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            if (pathMatcher.matches(file)) {
                result = file;
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return FileVisitResult.CONTINUE;
        }

        Path result() {
            return result;
        }
    }
}
