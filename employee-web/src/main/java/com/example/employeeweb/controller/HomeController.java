package com.example.employeeweb.controller;

import com.example.employeeweb.repository.CompanyRepository;
import com.example.employeeweb.repository.EmployeeRepository;
import com.example.employeeweb.model.Company;
import com.example.employeeweb.model.Employee;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;

    public HomeController(EmployeeRepository employeeRepository,
                          CompanyRepository companyRepository) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Employee> employees = employeeRepository.findAll();
        List<Company> companies = companyRepository.findAll();
        Map<Long, List<Employee>> employeesByCompany = new HashMap<>();
        for (Company company : companies) {
            employeesByCompany.put(company.getId(), employees.stream()
                    .filter(employee -> employee.getCompany() != null
                            && company.getId().equals(employee.getCompany().getId()))
                    .toList());
        }

        model.addAttribute("employees", employees);
        model.addAttribute("companies", companies);
        model.addAttribute("employeesByCompany", employeesByCompany);
        return "home";
    }
}
