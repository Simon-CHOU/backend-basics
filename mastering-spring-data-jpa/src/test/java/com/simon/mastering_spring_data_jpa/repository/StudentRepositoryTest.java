package com.simon.mastering_spring_data_jpa.repository;

import com.simon.mastering_spring_data_jpa.dao.Student;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class StudentRepositoryTest {
    @Autowired
    private StudentRepository studentRepository;

    @Test
    public void test_findByEmail() {
        List<Student> students = studentRepository.findByEmail("simon@gmail.com");
        assertEquals(1, students.size());
    }

    @Test
    public void test_findByAgeBetween() {
        List<Student> students = studentRepository.findByAgeBetween(18, 20);
        assertEquals(6, students.size());
    }


    @Test
    public void test_findByNameStartingWith() {
        List<Student> students = studentRepository.findByNameStartingWith("张");
        assertEquals(1, students.size());
    }

    @Test
    void test_findByEmailRawSql() {
        List<Student> students = studentRepository.findByEmailRawSql("xiaoli@example.com");
        assertEquals(1, students.size());
    }

    @Test
    void test_findByEmailRawSqlNative() {
        List<Student> students = studentRepository.findByEmailRawSqlNative("xiaoli@example.com");
        assertEquals(1, students.size());
    }

    @Test
    void test_findByEmailRawSqlPartial() {
        List<Student> students = studentRepository.findByEmailRawSqlPartial("xiaoli@example.com");
        assertNotNull(students.get(0).getName());
        assertNotNull(students.get(0).getEmail());
        assertNull(students.get(0).getAge());
        assertNull(students.get(0).getGender());
    }
}