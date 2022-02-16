package com.simon.callicoder.cplf;

public class UserService {
    public static User getUserDetail(String userId) {
        return new User(userId);
    }
//
//    public static <U> U getUserDetail(String userId) {
//    }

}
