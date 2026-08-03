package com.example.sqlscheduler.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Verifies classpath-based properties loading and input validation. */
class PropertiesLoaderTest {

    /** Verifies that an existing properties file is loaded from the test classpath. */
    @Test
    void shouldLoadPropertiesFromClasspath() throws IOException {
        Properties properties = new PropertiesLoader().load("test-application.properties");
        assertEquals("test-value", properties.getProperty("sample.key"));
    }

    /** Verifies that a leading slash is normalized before resolving the classpath resource. */
    @Test
    void shouldLoadPropertiesWhenResourceNameStartsWithSlash() throws IOException {
        Properties properties = new PropertiesLoader().load("/test-application.properties");
        assertEquals("test-value", properties.getProperty("sample.key"));
    }

    /** Verifies that a missing classpath resource produces an {@link IOException}. */
    @Test
    void shouldFailWhenResourceDoesNotExist() {
        assertThrows(IOException.class,
                () -> new PropertiesLoader().load("missing.properties"));
    }

    /** Verifies that blank resource names are rejected before any classpath lookup occurs. */
    @Test
    void shouldFailWhenResourceNameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new PropertiesLoader().load("   "));
    }
}
