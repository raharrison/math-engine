package uk.co.ryanharrison.mathengine.parser.spec;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import uk.co.ryanharrison.mathengine.utils.ResourceScanner;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads every spec file under {@code src/test/resources/engine} from the classpath.
 * <p>
 * Unknown JSON properties are rejected, so a typo in a fixture fails loudly instead
 * of silently disabling an assertion. The loaded suites are cached, because both the
 * spec runner and the integrity checks walk the same set.
 */
public final class SpecLoader {

    /**
     * Root resource directory holding the spec files.
     */
    public static final String ROOT = "engine";

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static List<LoadedSuite> cached;

    private SpecLoader() {
    }

    /**
     * A suite together with the resource path it came from, so failures can point at
     * the file that needs editing.
     *
     * @param resourcePath classpath path, for example {@code /engine/operators/add.json}
     * @param suite        the parsed suite
     */
    public record LoadedSuite(String resourcePath, SpecSuite suite) {

        /**
         * The file name without its directory or extension, for example {@code add}.
         */
        public String stem() {
            String name = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            return name.endsWith(".json") ? name.substring(0, name.length() - 5) : name;
        }

        /**
         * The directory the file sits in, for example {@code operators}.
         */
        public String group() {
            String withoutRoot = resourcePath.substring(("/" + ROOT + "/").length());
            int slash = withoutRoot.indexOf('/');
            return slash < 0 ? "" : withoutRoot.substring(0, slash);
        }
    }

    /**
     * Every spec suite on the classpath, in resource-path order.
     */
    public static synchronized List<LoadedSuite> loadAll() {
        if (cached == null) {
            cached = List.copyOf(scan());
        }
        return cached;
    }

    /**
     * Every case across every suite, in file order.
     */
    public static List<SpecCase> allCases() {
        List<SpecCase> cases = new ArrayList<>();
        loadAll().forEach(loaded -> cases.addAll(loaded.suite().tests()));
        return cases;
    }

    private static List<LoadedSuite> scan() {
        List<String> paths;
        try {
            paths = ResourceScanner.listResources(ROOT, ".json");
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot scan spec directory '" + ROOT + "'", e);
        }
        if (paths.isEmpty()) {
            throw new IllegalStateException("No spec files found under '" + ROOT + "'");
        }

        List<LoadedSuite> suites = new ArrayList<>(paths.size());
        for (String path : paths) {
            suites.add(new LoadedSuite(path, read(path)));
        }
        return suites;
    }

    private static SpecSuite read(String resourcePath) {
        try (InputStream is = SpecLoader.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Spec file disappeared from the classpath: " + resourcePath);
            }
            return MAPPER.readValue(is, SpecSuite.class);
        } catch (IOException e) {
            throw new UncheckedIOException("Cannot parse spec file " + resourcePath, e);
        }
    }
}
