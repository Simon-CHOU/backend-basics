package com.simon.mastering_spring_data_jpa.repository;

import com.simon.mastering_spring_data_jpa.dao.Student;
import com.simon.mastering_spring_data_jpa.dto.StudentDTO;
import jakarta.persistence.criteria.Predicate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
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

    @Test
    void test_dynamicQuery() {
        StudentDTO searchCriteria = StudentDTO.builder()
                .name("张三")
                .minAge(18)
                .maxAge(20)
                .build();

        Specification<Student> specification = buildStudentSpecification(searchCriteria);
        List<Student> foundStudents = studentRepository.findAll(specification);

        assertEquals(1, foundStudents.size());
        assertEquals("张三", foundStudents.get(0).getName());
    }

    private Specification<Student> buildStudentSpecification(StudentDTO criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getName() != null) {
                predicates.add(criteriaBuilder.equal(root.get("name"), criteria.getName()));
            }
            
            if (criteria.getMinAge() != 0) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("age"), criteria.getMinAge()));
            }
            
            if (criteria.getMaxAge() != 0) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("age"), criteria.getMaxAge()));
            }

            return query.where(predicates.toArray(new Predicate[0])).getRestriction();
        };
    }
}