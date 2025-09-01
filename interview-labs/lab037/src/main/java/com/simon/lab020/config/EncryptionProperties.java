package com.simon.lab020.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for file encryption
 */
@Data
@Component
@ConfigurationProperties(prefix = "file.encryption")
@Validated
public class EncryptionProperties {

    /**
     * Encryption algorithm (e.g., AES)
     */
    @NotBlank(message = "Encryption algorithm is required")
    private String algorithm = "AES";

    /**
     * Encryption key length in bits
     */
    @Positive(message = "Key length must be positive")
    private int keyLength = 256;

    /**
     * Encryption transformation (algorithm/mode/padding)
     */
    @NotBlank(message = "Encryption transformation is required")
    private String transformation = "AES/CBC/PKCS5Padding";

    /**
     * Master encryption key (for production, this should be externalized)
     */
    private String masterKey;

    /**
     * Salt for key derivation
     */
    private String salt = "FileStorageSalt2024";

    /**
     * Number of iterations for key derivation
     */
    @Positive(message = "Iterations must be positive")
    private int iterations = 10000;

    // Manual getters since Lombok is not working properly
    public String getAlgorithm() {
        return algorithm;
    }

    public int getKeyLength() {
        return keyLength;
    }

    public String getTransformation() {
        return transformation;
    }

    public String getMasterKey() {
        return masterKey;
    }

    public String getSalt() {
        return salt;
    }

    public int getIterations() {
        return iterations;
    }
}