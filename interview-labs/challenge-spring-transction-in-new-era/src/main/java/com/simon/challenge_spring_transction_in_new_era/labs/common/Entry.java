package com.simon.challenge_spring_transction_in_new_era.labs.common;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "entries")
public class Entry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String payload;

    @Column(nullable = false)
    private Instant createdAt;

    protected Entry() {}

    public Entry(String type, String payload, Instant createdAt) {
        this.type = type;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getType() { return type; }
    public String getPayload() { return payload; }
    public Instant getCreatedAt() { return createdAt; }
}
