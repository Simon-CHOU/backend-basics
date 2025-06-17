package com.simon.mastering_spring_data_jpa.repository;

import com.simon.mastering_spring_data_jpa.dao.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student, Long>, JpaSpecificationExecutor<Student> {
    // find 支持的关键字 And Between In StartingWith
    List<Student> findByEmail(String email);

    List<Student> findByAgeBetween(int minAge, int maxAge);

    List<Student> findByNameStartingWith(String namePrefix);
    //select by custom sql/jqpl
    @Query("select stu from Student stu where email = :email") // Student是对象名
    List<Student> findByEmailRawSql(@Param("email") String email);

    @Query(value = "select * from students stu where email = :email", nativeQuery = true) //students 是表明
    List<Student> findByEmailRawSqlNative(@Param("email") String email);

    @Query("select new Student(name, email) from Student where email = :email") // 查询部分字段，需有对应构造方法
    List<Student> findByEmailRawSqlPartial(@Param("email") String email);
}
