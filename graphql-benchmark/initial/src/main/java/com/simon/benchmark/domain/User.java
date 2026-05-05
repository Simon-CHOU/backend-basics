package com.simon.benchmark.domain;

import java.time.Instant;

public class User {

    private String userId;
    private String name;
    private String email;
    private String phone;
    private Instant registeredAt;

    public User() {
    }

    public User(String userId, String name, String email, String phone, Instant registeredAt) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.registeredAt = registeredAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Instant getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(Instant registeredAt) {
        this.registeredAt = registeredAt;
    }
}
