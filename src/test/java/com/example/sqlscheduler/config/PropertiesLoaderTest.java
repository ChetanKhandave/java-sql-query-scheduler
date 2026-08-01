package com.example.sqlscheduler.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertiesLoaderTest {

    @Test
    void shouldLoadPropertiesFromClasspath() throws IOException {
        Properties properties = new PropertiesLoader().load("test-application.properties");

        assertEquals("test-value", properties.getProperty("sample.key"));
    }

    @Test
    void shouldLoadPropertiesWhenResourceNameStartsWithSlash() throws IOException {
        Properties properties = new PropertiesLoader().load("/test-application.properties");

        assertEquals("test-value", properties.getProperty("sample.key"));
    }

    @Test
    void shouldFailWhenResourceDoesNotExist() {
        assertThrows(IOException.class,
                () -> new PropertiesLoader().load("missing.properties"));
    }

    @Test
    void shouldFailWhenResourceNameIsBlank() {
        assertThrows(IllegalArgumentException.class,
                () -> new PropertiesLoader().load("   "));
    }
}
