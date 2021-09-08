package com.simon.encryption.basic.core;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SymmetricDemo {
    public static void main(String[] args) throws NoSuchAlgorithmException {
//        Cipher cipher = Cipher.getInstance(algorithName);
//        Cipher cipher = Cipher.getInstance(algorithName, providerName);

        KeyGenerator keygen = KeyGenerator.getInstance("AES");
        SecureRandom random = new SecureRandom(); // see below
        keygen.init(random);
        Key key = keygen.generateKey();

        SecureRandom secrand = new SecureRandom();
        byte[] b = new byte[20];
        // fill with truly random bits
        secrand.setSeed(b);
    }
}
