package com.simon.mastering_spring_data_jpa.repository;

import com.simon.mastering_spring_data_jpa.dao.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface StudentRepository extends JpaRepository<Student,Long>, JpaSpecificationExecutor<Student> {
List<Student> findByEmail(String email);
List<Student> findByAgeBetween(int minAge, int maxAge);

}
