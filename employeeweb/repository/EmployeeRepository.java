package com.example.employeeweb.repository;

import com.example.employeeweb.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByCompany_Id(Long companyId);
    List<Employee> findByNameContainingIgnoreCase(String name);
}
