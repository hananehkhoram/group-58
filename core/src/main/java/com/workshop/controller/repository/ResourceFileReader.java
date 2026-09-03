package com.workshop.controller.repository;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

final class ResourceFileReader {

    private ResourceFileReader() {
    }

    static BufferedReader openUtf8(String filePath) throws IOException {
        String resourcePath = normalize(filePath);

        InputStream resourceStream = openResource(resourcePath);

        if (resourceStream != null) {
            return new BufferedReader(
                new InputStreamReader(
                    resourceStream,
                    StandardCharsets.UTF_8
                )
            );
        }

        List<Path> candidates = List.of(
            Path.of(filePath),
            Path.of("assets").resolve(resourcePath),
            Path.of("src", "main", "resources").resolve(resourcePath),
            Path.of("core", "src", "main", "resources").resolve(resourcePath)
        );

        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.newBufferedReader(
                    candidate,
                    StandardCharsets.UTF_8
                );
            }
        }

        throw new FileNotFoundException(
            "Could not find '" + filePath
                + "' as a classpath resource or file. "
                + "Current working directory: "
                + Path.of("").toAbsolutePath()
        );
    }

    private static InputStream openResource(String resourcePath) {
        ClassLoader contextLoader =
            Thread.currentThread().getContextClassLoader();

        InputStream stream = contextLoader == null
            ? null
            : contextLoader.getResourceAsStream(resourcePath);

        if (stream == null) {
            stream = ResourceFileReader.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);
        }

        return stream;
    }

    private static String normalize(String path) {
        String normalized = path.replace('\\', '/');

        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        return normalized;
    }
}
