package com.simon.mybatis.mapper;

import com.simon.mybatis.domain.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    void insert(User user);
    User findById(@Param("id") Long id);
    User findByEmail(@Param("email") String email);
    int updateNameById(@Param("id") Long id, @Param("name") String name);
    int deleteById(@Param("id") Long id);
    List<User> selectPageByNameLike(@Param("kw") String kw, @Param("offset") int offset, @Param("limit") int limit);
    long countUsers();
}

