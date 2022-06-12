package com.simon.mapper;

import com.simon.entity.User;

import java.util.List;

public interface UserMapper
{
    void insertUser(User user);
    User findUserById(Integer id);
    List<User> findAllUsers();
}
