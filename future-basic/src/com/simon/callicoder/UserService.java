package com.simon.callicoder;

public class UserService {
    public static User getUserDetail(String userId) {
        return new User(userId);
    }
//
//    public static <U> U getUserDetail(String userId) {
//    }

}
