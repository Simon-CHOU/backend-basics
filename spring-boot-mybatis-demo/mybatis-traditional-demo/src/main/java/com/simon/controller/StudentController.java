package com.simon.controller;

import com.simon.entity.Student;
import com.simon.service.StudentService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * https://programmer.help/blogs/spring-boot-tutorial-mybatis.html
 */
@RestController
@RequestMapping("/student")
public class StudentController {

    @Resource
    private StudentService studentService;

    /**
     * curl --location --request GET 'http://127.0.0.1:9998/student/findAll'
     */
    @RequestMapping("/findAll")
    public List<Student> findAll() {
        return studentService.findAll();
    }

    /**
     * curl --location --request GET 'http://127.0.0.1:9998/student/findOne' --form 'id="1"'
     */
    @RequestMapping("/findOne")
    public Student findOne(Long id) {
        return studentService.findOne(id);
    }

    /**
     * curl --location --request GET 'http://127.0.0.1:9998/student/findByStudentNo' --form 'studentNo="80097"'
     */
    @RequestMapping("/findByStudentNo")
    public Student findByStudentNo(String studentNo) {
        return studentService.findByStudentNo(studentNo);
    }

}