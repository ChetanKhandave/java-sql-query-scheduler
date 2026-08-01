package com.example.sqlscheduler.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Loads Java {@link Properties} from a classpath resource.
 *
 * <p>The defining class loader is tried first for predictable behavior across Maven and IDE
 * test runners. The thread context class loader is used as a fallback for container-style
 * environments.</p>
 */
public final class PropertiesLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesLoader.class);

    /**
     * Loads a properties file from the application classpath.
     *
     * @param resourceName classpath resource name, with or without a leading slash
     * @return loaded properties
     * @throws IOException when the resource cannot be found or read
     */
    public Properties load(String resourceName) throws IOException {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            throw new IllegalArgumentException("resourceName must not be blank");
        }

        String normalizedResourceName = normalize(resourceName);
        LOGGER.debug("Loading classpath properties resource: {}", normalizedResourceName);
        Properties properties = new Properties();

        try (InputStream inputStream = openResource(normalizedResourceName)) {
            if (inputStream == null) {
                LOGGER.error("Classpath properties resource was not found: {}", normalizedResourceName);
                throw new IOException(
                        "Configuration resource not found on classpath: " + normalizedResourceName);
            }
            properties.load(inputStream);
        }

        LOGGER.info("Loaded {} configuration properties from {}",
                properties.size(), normalizedResourceName);
        return properties;
    }

    private InputStream openResource(String resourceName) {
        ClassLoader definingClassLoader = PropertiesLoader.class.getClassLoader();
        if (definingClassLoader != null) {
            InputStream inputStream = definingClassLoader.getResourceAsStream(resourceName);
            if (inputStream != null) {
                LOGGER.debug("Resource {} resolved by defining class loader", resourceName);
                return inputStream;
            }
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null && contextClassLoader != definingClassLoader) {
            InputStream inputStream = contextClassLoader.getResourceAsStream(resourceName);
            if (inputStream != null) {
                LOGGER.debug("Resource {} resolved by thread context class loader", resourceName);
            }
            return inputStream;
        }

        return null;
    }

    private String normalize(String resourceName) {
        String normalizedResourceName = resourceName.trim();
        while (normalizedResourceName.startsWith("/")) {
            normalizedResourceName = normalizedResourceName.substring(1);
        }
        return normalizedResourceName;
    }
}
