package com.simon.repository;

import com.simon.model.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class EmployeeMyBatisRepositoryTest {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private EmployeeMyBatisRepository employeeRepository;

    @Test
    @Transactional // 加 Transactional 回滚成功
//    @Rollback // 方法上加 Rollback并不会回滚
    public void test() {

        logger.info("Inserting -> {}", employeeRepository.insert(new Employee(10011L, "Ramesh", "Fadatare", "ramesh@gmail.com")));
        logger.info("Inserting -> {}", employeeRepository.insert(new Employee(10012L, "John", "Cena", "john@gmail.com")));
        logger.info("Inserting -> {}", employeeRepository.insert(new Employee(10013L, "tony", "stark", "stark@gmail.com")));

        logger.info("Employee id 10011 -> {}", employeeRepository.findById(10011L));

        logger.info("Update 10003 -> {}", employeeRepository.update(new Employee(10011L, "ram", "Stark", "ramesh123@gmail.com")));

        employeeRepository.deleteById(10013L);

        List<Employee> all = employeeRepository.findAll();
        assertEquals("ramesh123@gmail.com", all.get(1).getEmailId());
        logger.info("All users -> {}", all);
    }
}