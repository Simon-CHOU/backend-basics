package com.simon.migrateswagger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class TestEncoding {
    public static void main(String[] args) throws UnsupportedEncodingException {
        System.out.println(StandardCharsets.ISO_8859_1.name());
        System.out.println(StandardCharsets.ISO_8859_1.encode("下载"));
        System.out.println(URLEncoder.encode("下载",StandardCharsets.ISO_8859_1.name()));
        System.out.println(URLEncoder.encode("下载",StandardCharsets.UTF_8.name()));
    }
}
