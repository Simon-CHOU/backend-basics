package com.simon.jpa.spec;

import com.simon.jpa.domain.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecs {
    public static Specification<User> nameContains(String kw) {
        return (root, q, cb) -> cb.like(root.get("name"), "%" + kw + "%");
    }
}

