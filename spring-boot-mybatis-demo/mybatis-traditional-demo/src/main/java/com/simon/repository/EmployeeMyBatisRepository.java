package com.simon.repository;

import com.simon.model.Employee;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.type.JdbcType;

import java.util.List;

/**
 * 由于数据表字段名和类属性名不一致（前者是下划线，后者是大驼峰）
 * 所以需要使用@Result注解进行映射，否则属性值为null
 * https://blog.csdn.net/qq_38319289/article/details/108257776
 */
@Mapper
public interface EmployeeMyBatisRepository {
    @Select("select * from employees")
    @Results({
            @Result(column = "first_name", property = "firstName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "last_name", property = "lastName", jdbcType = JdbcType.VARCHAR),
            @Result(column = "email_address", property = "emailId", jdbcType = JdbcType.VARCHAR)
    })
    public List<Employee> findAll();

    @Select("SELECT * FROM employees WHERE id = #{id}")
    public Employee findById(long id);

    @Delete("DELETE FROM employees WHERE id = #{id}")
    public int deleteById(long id);

    @Insert("INSERT INTO employees(id, first_name, last_name,email_address) " +
            " VALUES (#{id}, #{firstName}, #{lastName}, #{emailId})")
    public int insert(Employee employee);

    @Update("Update employees set first_name=#{firstName}, " +
            " last_name=#{lastName}, email_address=#{emailId} where id=#{id}")
    public int update(Employee employee);
}