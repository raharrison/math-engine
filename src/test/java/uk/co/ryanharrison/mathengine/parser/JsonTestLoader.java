package uk.co.ryanharrison.mathengine.parser;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility class for loading test cases from JSON files.
 */
public class JsonTestLoader {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Load a test suite from a JSON file in the classpath.
     */
    public static JsonTestSuite loadFromResource(String resourcePath) throws IOException {
        try (InputStream is = JsonTestLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }
            return objectMapper.readValue(is, JsonTestSuite.class);
        }
    }

    /**
     * Load a test suite from a JSON file on the filesystem.
     */
    public static JsonTestSuite loadFromFile(Path path) throws IOException {
        return objectMapper.readValue(path.toFile(), JsonTestSuite.class);
    }

    /**
     * Load all test suites from a directory.
     */
    public static List<JsonTestSuite> loadAllFromDirectory(Path directory) throws IOException {
        List<JsonTestSuite> suites = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(directory)) {
            List<Path> jsonFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .toList();

            for (Path path : jsonFiles) {
                suites.add(loadFromFile(path));
            }
        }

        return suites;
    }

    /**
     * Load all test cases from multiple suites.
     */
    public static List<JsonTestCase> loadAllTestCases(List<JsonTestSuite> suites) {
        return suites.stream()
                .flatMap(suite -> suite.tests().stream())
                .collect(Collectors.toList());
    }
}
