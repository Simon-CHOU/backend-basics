package com.simon.lab020.service;

import com.simon.lab020.config.EncryptionProperties;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for file encryption and decryption operations
 */
@Service
@RequiredArgsConstructor
public class EncryptionService {
    private static final Logger log = LoggerFactory.getLogger(EncryptionService.class);

    private final EncryptionProperties encryptionProperties;
    private static final int IV_LENGTH = 16; // 128 bits

    /**
     * Generate a new encryption key
     *
     * @return Base64 encoded encryption key
     */
    public String generateEncryptionKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(encryptionProperties.getAlgorithm());
            keyGenerator.init(encryptionProperties.getKeyLength());
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            log.error("Error generating encryption key", e);
            throw new RuntimeException("Failed to generate encryption key", e);
        }
    }

    /**
     * Encrypt a file
     *
     * @param inputFile the input file to encrypt
     * @param outputFile the output file for encrypted data
     * @param encryptionKey the encryption key (Base64 encoded)
     * @return the initialization vector used (Base64 encoded)
     */
    public String encryptFile(File inputFile, File outputFile, String encryptionKey) {
        try {
            // Generate random IV
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Create cipher
            Cipher cipher = Cipher.getInstance(encryptionProperties.getTransformation());
            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey), 
                encryptionProperties.getAlgorithm()
            );
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // Encrypt file
            try (FileInputStream fis = new FileInputStream(inputFile);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                
                // Write IV to the beginning of the encrypted file
                fos.write(iv);
                
                // Encrypt and write data
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] encryptedData = cipher.update(buffer, 0, bytesRead);
                    if (encryptedData != null) {
                        fos.write(encryptedData);
                    }
                }
                
                // Write final block
                byte[] finalBlock = cipher.doFinal();
                if (finalBlock != null) {
                    fos.write(finalBlock);
                }
            }

            log.debug("File encrypted successfully: {} -> {}", inputFile.getName(), outputFile.getName());
            return Base64.getEncoder().encodeToString(iv);
            
        } catch (Exception e) {
            log.error("Error encrypting file: {}", inputFile.getName(), e);
            throw new RuntimeException("Failed to encrypt file", e);
        }
    }

    /**
     * Decrypt a file
     *
     * @param inputFile the encrypted input file
     * @param outputFile the output file for decrypted data
     * @param encryptionKey the encryption key (Base64 encoded)
     */
    public void decryptFile(File inputFile, File outputFile, String encryptionKey) {
        try {
            // Create cipher
            Cipher cipher = Cipher.getInstance(encryptionProperties.getTransformation());
            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey), 
                encryptionProperties.getAlgorithm()
            );

            try (FileInputStream fis = new FileInputStream(inputFile);
                 FileOutputStream fos = new FileOutputStream(outputFile)) {
                
                // Read IV from the beginning of the file
                byte[] iv = new byte[IV_LENGTH];
                int ivBytesRead = fis.read(iv);
                if (ivBytesRead != IV_LENGTH) {
                    throw new IOException("Failed to read IV from encrypted file");
                }
                
                // Initialize cipher with IV
                IvParameterSpec ivSpec = new IvParameterSpec(iv);
                cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
                
                // Process remaining encrypted data in chunks
                byte[] buffer = new byte[8192];
                int bytesRead;
                
                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] decryptedData = cipher.update(buffer, 0, bytesRead);
                    if (decryptedData != null) {
                        fos.write(decryptedData);
                    }
                }
                
                // Write final block
                byte[] finalBlock = cipher.doFinal();
                if (finalBlock != null) {
                    fos.write(finalBlock);
                }
            }

            log.debug("File decrypted successfully: {} -> {}", inputFile.getName(), outputFile.getName());
            
        } catch (Exception e) {
            log.error("Error decrypting file: {}", inputFile.getName(), e);
            throw new RuntimeException("Failed to decrypt file", e);
        }
    }

    /**
     * Encrypt data in memory
     *
     * @param data the data to encrypt
     * @param encryptionKey the encryption key (Base64 encoded)
     * @return encrypted data with IV prepended (Base64 encoded)
     */
    public String encryptData(byte[] data, String encryptionKey) {
        try {
            // Generate random IV
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // Create cipher
            Cipher cipher = Cipher.getInstance(encryptionProperties.getTransformation());
            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey), 
                encryptionProperties.getAlgorithm()
            );
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // Encrypt data
            byte[] encryptedData = cipher.doFinal(data);
            
            // Combine IV and encrypted data
            byte[] result = new byte[IV_LENGTH + encryptedData.length];
            System.arraycopy(iv, 0, result, 0, IV_LENGTH);
            System.arraycopy(encryptedData, 0, result, IV_LENGTH, encryptedData.length);
            
            return Base64.getEncoder().encodeToString(result);
            
        } catch (Exception e) {
            log.error("Error encrypting data", e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    /**
     * Encrypt string data in memory
     *
     * @param data the string data to encrypt
     * @param encryptionKey the encryption key (Base64 encoded)
     * @return encrypted data with IV prepended (Base64 encoded)
     */
    public String encryptData(String data, String encryptionKey) {
        return encryptData(data.getBytes(StandardCharsets.UTF_8), encryptionKey);
    }

    /**
     * Decrypt data in memory
     *
     * @param encryptedData the encrypted data with IV prepended (Base64 encoded)
     * @param encryptionKey the encryption key (Base64 encoded)
     * @return decrypted data
     */
    public byte[] decryptData(String encryptedData, String encryptionKey) {
        try {
            byte[] data = Base64.getDecoder().decode(encryptedData);
            
            // Extract IV
            byte[] iv = new byte[IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, IV_LENGTH);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            
            // Extract encrypted data
            byte[] encrypted = new byte[data.length - IV_LENGTH];
            System.arraycopy(data, IV_LENGTH, encrypted, 0, encrypted.length);

            // Create cipher
            Cipher cipher = Cipher.getInstance(encryptionProperties.getTransformation());
            SecretKeySpec keySpec = new SecretKeySpec(
                Base64.getDecoder().decode(encryptionKey), 
                encryptionProperties.getAlgorithm()
            );
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            return cipher.doFinal(encrypted);
            
        } catch (Exception e) {
            log.error("Error decrypting data", e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    /**
     * Decrypt data in memory and return as string
     *
     * @param encryptedData the encrypted data with IV prepended (Base64 encoded)
     * @param encryptionKey the encryption key (Base64 encoded)
     * @return decrypted string data
     */
    public String decryptDataAsString(String encryptedData, String encryptionKey) {
        byte[] decryptedBytes = decryptData(encryptedData, encryptionKey);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }
}