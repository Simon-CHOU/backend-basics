package com.simon.lab020.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Test class for FileUploadProperties
 */
@SpringBootTest
@ActiveProfiles("test")
class FileUploadPropertiesTest {

    private FileUploadProperties properties;

    @BeforeEach
    void setUp() {
        properties = new FileUploadProperties();
    }

    @Test
    void testDefaultValues() {
        assertThat(properties.getChunkSize()).isEqualTo("10MB");
        assertThat(properties.getSessionTimeout()).isEqualTo("24h");
        assertThat(properties.getMaxConcurrentChunks()).isEqualTo(5);
    }

    @Test
    void testGetChunkSizeInBytes_MB() {
        properties.setChunkSize("10MB");
        assertThat(properties.getChunkSizeInBytes()).isEqualTo(10 * 1024 * 1024);
    }

    @Test
    void testGetChunkSizeInBytes_KB() {
        properties.setChunkSize("512KB");
        assertThat(properties.getChunkSizeInBytes()).isEqualTo(512 * 1024);
    }

    @Test
    void testGetChunkSizeInBytes_GB() {
        properties.setChunkSize("1GB");
        assertThat(properties.getChunkSizeInBytes()).isEqualTo(1024L * 1024 * 1024);
    }

    @Test
    void testGetChunkSizeInBytes_Bytes() {
        properties.setChunkSize("1024B");
        assertThat(properties.getChunkSizeInBytes()).isEqualTo(1024);
    }

    @Test
    void testGetChunkSizeInBytes_PlainNumber() {
        properties.setChunkSize("1024");
        assertThat(properties.getChunkSizeInBytes()).isEqualTo(1024);
    }

    @Test
    void testGetChunkSizeInBytes_WithSpaces() {
        properties.setChunkSize(" 10 MB ");
        assertThat(properties.getChunkSizeInBytes()).isEqualTo(10 * 1024 * 1024);
    }

    @Test
    void testGetChunkSizeInBytes_LowerCase() {
        properties.setChunkSize("10mb");
        assertThat(properties.getChunkSizeInBytes()).isEqualTo(10 * 1024 * 1024);
    }

    @Test
    void testGetChunkSizeInBytes_NullValue() {
        properties.setChunkSize(null);
        assertThatThrownBy(() -> properties.getChunkSizeInBytes())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Size cannot be null or empty");
    }

    @Test
    void testGetChunkSizeInBytes_EmptyValue() {
        properties.setChunkSize("");
        assertThatThrownBy(() -> properties.getChunkSizeInBytes())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Size cannot be null or empty");
    }

    @Test
    void testGetChunkSizeInBytes_InvalidFormat() {
        properties.setChunkSize("invalidMB");
        assertThatThrownBy(() -> properties.getChunkSizeInBytes())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid size format");
    }

    @Test
    void testGetSessionTimeoutDuration_Hours() {
        properties.setSessionTimeout("24h");
        assertThat(properties.getSessionTimeoutDuration()).isEqualTo(Duration.ofHours(24));
    }

    @Test
    void testGetSessionTimeoutDuration_Minutes() {
        properties.setSessionTimeout("30m");
        assertThat(properties.getSessionTimeoutDuration()).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void testGetSessionTimeoutDuration_Seconds() {
        properties.setSessionTimeout("60s");
        assertThat(properties.getSessionTimeoutDuration()).isEqualTo(Duration.ofSeconds(60));
    }

    @Test
    void testGetSessionTimeoutDuration_Days() {
        properties.setSessionTimeout("7d");
        assertThat(properties.getSessionTimeoutDuration()).isEqualTo(Duration.ofDays(7));
    }

    @Test
    void testGetSessionTimeoutDuration_PlainNumber() {
        properties.setSessionTimeout("3600");
        assertThat(properties.getSessionTimeoutDuration()).isEqualTo(Duration.ofSeconds(3600));
    }

    @Test
    void testGetSessionTimeoutDuration_WithSpaces() {
        properties.setSessionTimeout(" 24 h ");
        assertThatThrownBy(() -> properties.getSessionTimeoutDuration())
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void testGetSessionTimeoutDuration_NullValue() {
        properties.setSessionTimeout(null);
        assertThatThrownBy(() -> properties.getSessionTimeoutDuration())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duration cannot be null or empty");
    }

    @Test
    void testGetSessionTimeoutDuration_EmptyValue() {
        properties.setSessionTimeout("");
        assertThatThrownBy(() -> properties.getSessionTimeoutDuration())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Duration cannot be null or empty");
    }

    @Test
    void testGetSessionTimeoutDuration_InvalidFormat() {
        properties.setSessionTimeout("invalidh");
        assertThatThrownBy(() -> properties.getSessionTimeoutDuration())
                .isInstanceOf(NumberFormatException.class);
    }

    @Test
    void testSettersAndGetters() {
        properties.setChunkSize("5MB");
        properties.setSessionTimeout("12h");
        properties.setMaxConcurrentChunks(10);

        assertThat(properties.getChunkSize()).isEqualTo("5MB");
        assertThat(properties.getSessionTimeout()).isEqualTo("12h");
        assertThat(properties.getMaxConcurrentChunks()).isEqualTo(10);
    }
}