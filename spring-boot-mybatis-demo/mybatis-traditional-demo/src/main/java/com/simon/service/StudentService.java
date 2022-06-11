package com.simon.service;

import com.simon.entity.Student;

import java.util.List;

public interface StudentService {

    List<Student> findAll();

    Student findOne(Long id);

    Student findByStudentNo(String studentNo);
}