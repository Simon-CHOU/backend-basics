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
    @Query("select stu from student stu where email = :email")
    List<Student> findByEmailRawSql(@Param("email") String email);
}
