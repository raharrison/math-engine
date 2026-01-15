package uk.co.ryanharrison.mathengine.utils;

import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

public final class ResourceScanner {

    private ResourceScanner() {
    }

    public static List<String> listResources(String directory, String suffix) throws IOException {
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

        URL url = directory == null || directory.isEmpty()
                ? cl.getResource(".")
                : cl.getResource(directory);

        if (url == null) {
            throw new IllegalArgumentException("Resource directory not found: " + directory);
        }

        return switch (url.getProtocol()) {
            case "file" -> listFromFileSystem(url, directory, suffix);
            case "jar" -> listFromJar(url, directory, suffix);
            default -> throw new UnsupportedOperationException(
                    "Unsupported protocol: " + url.getProtocol());
        };
    }

    /* ================= filesystem ================= */

    private static List<String> listFromFileSystem(
            URL url, String directory, String suffix) throws IOException {

        Path root;
        try {
            root = Paths.get(url.toURI());
        } catch (Exception e) {
            throw new IOException(e);
        }

        try (Stream<Path> paths = Files.walk(root)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().endsWith(suffix))
                    .map(p -> toResourcePath(root, p, directory))
                    .sorted()
                    .toList();
        }
    }

    private static String toResourcePath(Path root, Path file, String directory) {
        String rel = root.relativize(file).toString().replace("\\", "/");
        return directory == null || directory.isEmpty()
                ? "/" + rel
                : "/" + directory + "/" + rel;
    }

    /* ================= jar ================= */

    private static List<String> listFromJar(
            URL url, String directory, String suffix) throws IOException {

        String path = url.getPath(); // jar:file:/.../x.jar!/<dir>
        String jarPath = path.substring(5, path.indexOf("!"));

        List<String> result = new ArrayList<>();

        try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8))) {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                String name = e.getName();

                boolean inDir = directory == null || directory.isEmpty()
                        || name.startsWith(directory + "/");

                if (inDir && name.endsWith(suffix)) {
                    result.add("/" + name);
                }
            }
        }

        Collections.sort(result);
        return result;
    }
}

