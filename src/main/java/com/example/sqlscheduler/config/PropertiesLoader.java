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

        Properties properties = new Properties();
        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream(resourceName)) {
            if (inputStream == null) {
                throw new IOException("Configuration resource not found: " + resourceName);
            }
            properties.load(inputStream);
        }
        return properties;
    }
}
