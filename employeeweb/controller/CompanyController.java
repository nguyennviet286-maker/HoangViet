package com.example.employeeweb.controller;

import com.example.employeeweb.model.Company;
import com.example.employeeweb.model.Employee;
import com.example.employeeweb.repository.CompanyRepository;
import com.example.employeeweb.repository.EmployeeRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final EmployeeRepository employeeRepository;

    public CompanyController(CompanyRepository companyRepository,
                             EmployeeRepository employeeRepository) {
        this.companyRepository = companyRepository;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/companies")
    public String listCompanies(Model model) {
        model.addAttribute("companies", companyRepository.findAll());
        return "companyList";
    }

    @GetMapping("/companies/new")
    public String newCompany(Model model) {
        model.addAttribute("company", new Company());
        return "companyForm";
    }

    @GetMapping("/companies/{id}/edit")
    public String editCompany(@PathVariable Long id, Model model) {
        model.addAttribute("company", findCompany(id));
        return "companyForm";
    }

    @PostMapping("/companies/save")
    public String saveCompany(@Valid @ModelAttribute("company") Company company,
                              BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "companyForm";
        }
        companyRepository.save(company);
        return "redirect:/companies";
    }

    @PostMapping("/companies/{id}/delete")
    public String deleteCompany(@PathVariable Long id) {
        Company company = findCompany(id);
        List<Employee> employees = employeeRepository.findByCompany_Id(id);
        employees.forEach(employee -> employee.setCompany(null));
        employeeRepository.saveAll(employees);
        companyRepository.delete(company);
        return "redirect:/companies";
    }

    @GetMapping("/companies/{id}")
    public String getCompanyById(@PathVariable Long id, Model model) {
        Company company = findCompany(id);
        List<Employee> employees = employeeRepository.findByCompany_Id(id);

        model.addAttribute("employees", employees);
        model.addAttribute("company", company);
        return "companyDetail";
    }

    private Company findCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Company not found"));
    }
}
