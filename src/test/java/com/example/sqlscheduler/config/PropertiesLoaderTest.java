package com.example.sqlscheduler.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class PropertiesLoaderTest {

    @Test
    void shouldLoadPropertiesFromClasspath() throws IOException {
        Properties properties = new PropertiesLoader().load("test-application.properties");
        assertEquals("test-value", properties.getProperty("sample.key"));
    }

    @Test
    void shouldFailWhenResourceDoesNotExist() {
        assertThrows(IOException.class,
                () -> new PropertiesLoader().load("missing.properties"));
    }
}
