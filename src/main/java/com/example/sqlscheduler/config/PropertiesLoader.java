package com.example.sqlscheduler.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/** Loads configuration from a classpath resource. */
public final class PropertiesLoader {

    public Properties load(String resourceName) throws IOException {
        if (resourceName == null || resourceName.trim().isEmpty()) {
            throw new IllegalArgumentException("resourceName must not be blank");
        }

        String normalizedResourceName = normalize(resourceName);
        Properties properties = new Properties();

        try (InputStream inputStream = openResource(normalizedResourceName)) {
            if (inputStream == null) {
                throw new IOException(
                        "Configuration resource not found on classpath: " + normalizedResourceName);
            }
            properties.load(inputStream);
        }

        return properties;
    }

    private InputStream openResource(String resourceName) {
        ClassLoader definingClassLoader = PropertiesLoader.class.getClassLoader();
        if (definingClassLoader != null) {
            InputStream inputStream = definingClassLoader.getResourceAsStream(resourceName);
            if (inputStream != null) {
                return inputStream;
            }
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null && contextClassLoader != definingClassLoader) {
            return contextClassLoader.getResourceAsStream(resourceName);
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
