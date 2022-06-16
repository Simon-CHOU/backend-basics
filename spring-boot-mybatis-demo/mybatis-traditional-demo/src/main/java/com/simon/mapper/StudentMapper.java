package com.simon.mapper;

import com.simon.entity.Student;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StudentMapper {

    /**
     * Query all student information
     * @return List<Student>
     */
    List<Student> findAll();

    /**
     * Query student information by id
     * @param id
     * @return Student
     */
    Student findOne(Long id);

    /**
     * Query student information through student ID
     * @param studentNo
     * @return Student
     */
    Student findByStudentNo(String studentNo);

}