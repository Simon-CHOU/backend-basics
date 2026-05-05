package com.simon.benchmark.infra;

import com.simon.benchmark.domain.User;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {

    private static final Map<String, User> STORE = new ConcurrentHashMap<>();

    static {
        for (int i = 1; i <= 200; i++) {
            String userId = "USR-" + String.format("%04d", i);
            User user = new User(
                    userId,
                    "User Name " + i,
                    "user" + i + "@example.com",
                    "+1-555-" + String.format("%04d", i),
                    Instant.parse("2024-01-15T08:00:00Z")
            );
            STORE.put(userId, user);
        }
    }

    public User findById(String userId) {
        return STORE.get(userId);
    }
}
