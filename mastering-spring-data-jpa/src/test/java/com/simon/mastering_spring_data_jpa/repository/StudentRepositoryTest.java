package com.simon.mastering_spring_data_jpa.repository;

import com.simon.mastering_spring_data_jpa.dao.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StudentRepositoryTest {
    @Autowired
    private StudentRepository studentRepository;

    @Test
    public void findByEmail() {
        List<Student> students = studentRepository.findByEmail("simon@gmail.com");
        assertEquals(1, students.size());
    }

    @Test
    public void findByAgeBetween() {
        List<Student> students = studentRepository.findByAgeBetween(18, 20);
        assertEquals(6, students.size());
    }
}