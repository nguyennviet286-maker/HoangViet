package com.example.employeeweb.repository;

import com.example.employeeweb.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
