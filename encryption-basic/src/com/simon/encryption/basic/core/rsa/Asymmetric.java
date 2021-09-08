package com.simon.encryption.basic.core.rsa;

import java.security.*;

public class Asymmetric {
    private static final int KEYSIZE = 1024;// todo 数值不对时，初始化会报错： Invalid key sizes
    public static void main(String[] args) throws NoSuchAlgorithmException {
        KeyPairGenerator pairgen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = new SecureRandom();
        pairgen.initialize(KEYSIZE, random);
        KeyPair keyPair = pairgen.generateKeyPair();
        Key publicKey = keyPair.getPublic();
        Key privateKey = keyPair.getPrivate();
    }
}
