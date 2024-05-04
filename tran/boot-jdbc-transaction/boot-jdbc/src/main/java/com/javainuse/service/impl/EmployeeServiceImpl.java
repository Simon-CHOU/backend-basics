package com.javainuse.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.javainuse.dao.EmployeeDao;
import com.javainuse.model.Employee;
import com.javainuse.service.EmployeeService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeServiceImpl implements EmployeeService {

	@Autowired
	EmployeeDao employeeDao;

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public void insertEmployee(Employee employee) {
		employeeDao.insertEmployee(employee);
	}

	@Override
	public void deleteEmployeeById(String empid) {
		employeeDao.deleteEmployeeById(empid);
	}

}