package com.simon.mybatis.mapper;

import com.simon.mybatis.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    void insert(User user);
    void insertUser(User user);
    User findById(@Param("id") Long id);
    User findUserById(@Param("id") Long id);
    User findByEmail(@Param("email") String email);
    int updateNameById(@Param("id") Long id, @Param("name") String name);
    int updateUser(User user);
    int deleteById(@Param("id") Long id);
    int deleteAllUsers();
    int deleteUsersByIdRange(@Param("startId") long startId, @Param("endId") long endId);
    List<User> selectPageByNameLike(@Param("kw") String kw, @Param("offset") int offset, @Param("limit") int limit);
    List<User> findUsersByNameLike(@Param("name") String name);
    List<User> findUsersByEmailLike(@Param("email") String email);
    List<User> findUsersByConditions(@Param("name") String name, @Param("email") String email);
    List<User> findAllUsers();
    List<User> findUsersWithPagination(@Param("limit") int limit, @Param("offset") int offset);
    long countUsers();
}

