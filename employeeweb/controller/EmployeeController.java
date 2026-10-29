package com.example.employeeweb.controller;

import com.example.employeeweb.model.Employee;
import com.example.employeeweb.repository.CompanyRepository;
import com.example.employeeweb.repository.EmployeeRepository;
import com.example.employeeweb.service.ImageStorageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import com.example.employeeweb.model.Company;
import java.util.List;

@Controller
public class EmployeeController {
    private final EmployeeRepository employeeRepository;
    private final CompanyRepository companyRepository;
    private final ImageStorageService imageStorageService;

    public EmployeeController(EmployeeRepository employeeRepository,
                              CompanyRepository companyRepository,
                              ImageStorageService imageStorageService) {
        this.employeeRepository = employeeRepository;
        this.companyRepository = companyRepository;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/employees")
    public String listEmployees(Model model) {
        model.addAttribute("employees", employeeRepository.findAll());
        model.addAttribute("searchName", "");
        return "employeeList";
    }

    @GetMapping("/employees/search")
    public String searchEmployees(@RequestParam(value = "name", defaultValue = "") String name,
                                  Model model) {
        String searchName = name.trim();
        model.addAttribute("employees", searchName.isEmpty()
                ? employeeRepository.findAll()
                : employeeRepository.findByNameContainingIgnoreCase(searchName));
        model.addAttribute("searchName", name);
        return "employeeList";
    }

    @GetMapping("/employees/sort/asc")
    public String sortEmployeesAscending(Model model) {
        model.addAttribute("employees", employeeRepository.findAll(Sort.by(Sort.Direction.ASC, "name")));
        model.addAttribute("searchName", "");
        return "employeeList";
    }

    @GetMapping("/employees/sort/desc")
    public String sortEmployeesDescending(Model model) {
        model.addAttribute("employees", employeeRepository.findAll(Sort.by(Sort.Direction.DESC, "name")));
        model.addAttribute("searchName", "");
        return "employeeList";
    }

    @GetMapping("/employees/{id}")
    public String employeeDetail(@PathVariable Long id, Model model) {
        model.addAttribute("employee", findEmployee(id));
        return "employeeDetail";
    }

    @GetMapping("/employees/new")
    public String newEmployee(Model model) {
        model.addAttribute("employee", new Employee());
        addCompanies(model);
        model.addAttribute("pageTitle", "Add employee");
        return "employeeForm";
    }

    @GetMapping("/employees/{id}/edit")
    public String editEmployee(@PathVariable Long id, Model model) {
        Employee employee = findEmployee(id);
        model.addAttribute("employee", employee);
        model.addAttribute("companyId", employee.getCompany() == null ? null : employee.getCompany().getId());
        model.addAttribute("imageUrl", isHttpImageUrl(employee.getImage()) ? employee.getImage() : "");
        addCompanies(model);
        model.addAttribute("pageTitle", "Edit employee");
        return "employeeForm";
    }

    @PostMapping("/employees/save")
    public String saveEmployee(
            @Valid @ModelAttribute("employee") Employee employee,
            BindingResult bindingResult,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @RequestParam(value = "imageUrl", required = false) String imageUrl,
            @RequestParam(value = "companyId", required = false) Long companyId,
            Model model) {

        if (bindingResult.hasErrors()) {
            if (employee.getId() != null) {
                Employee existingEmployee = findEmployee(employee.getId());
                employee.setImage(existingEmployee.getImage());
                if ((imageUrl == null || imageUrl.isBlank())
                        && isHttpImageUrl(existingEmployee.getImage())) {
                    imageUrl = existingEmployee.getImage();
                }
            }
            model.addAttribute("companyId", companyId);
            model.addAttribute("imageUrl", imageUrl);
            addCompanies(model);
            model.addAttribute("pageTitle",
                    employee.getId() == null ? "Add employee" : "Edit employee");
            return "employeeForm";
        }

        // Keep the existing image when editing without choosing a new one.
        if (employee.getId() != null) {
            Employee existingEmployee = findEmployee(employee.getId());
            employee.setImage(existingEmployee.getImage());
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                employee.setImage(imageStorageService.store(imageFile));
            } catch (IOException | IllegalArgumentException ex) {
                model.addAttribute("pageTitle",
                        employee.getId() == null ? "Add employee" : "Edit employee");
                model.addAttribute("uploadError", ex.getMessage());
                model.addAttribute("companyId", companyId);
                model.addAttribute("imageUrl", imageUrl);
                addCompanies(model);
                return "employeeForm";
            }
        } else if (imageUrl != null && !imageUrl.isBlank()) {
            String normalizedImageUrl = imageUrl.trim();
            if (!isHttpImageUrl(normalizedImageUrl)) {
                model.addAttribute("pageTitle",
                        employee.getId() == null ? "Add employee" : "Edit employee");
                model.addAttribute("uploadError", "Image link must be a valid HTTP or HTTPS URL.");
                model.addAttribute("companyId", companyId);
                model.addAttribute("imageUrl", imageUrl);
                addCompanies(model);
                return "employeeForm";
            }
            employee.setImage(normalizedImageUrl);
        }

        employee.setCompany(companyId == null ? null : companyRepository.findById(companyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Company not found")));

        employeeRepository.save(employee);
        return "redirect:/employees";
    }

    @PostMapping("/employees/{id}/delete")
    public String deleteEmployee(@PathVariable Long id) {
        employeeRepository.delete(findEmployee(id));
        return "redirect:/employees";
    }

    private Employee findEmployee(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found"));
    }

    private void addCompanies(Model model) {
        List<Company> companies = companyRepository.findAll();
        model.addAttribute("companies", companies);
    }

    private boolean isHttpImageUrl(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        try {
            URI uri = URI.create(value.trim());
            return uri.isAbsolute()
                    && uri.getHost() != null
                    && ("http".equalsIgnoreCase(uri.getScheme())
                    || "https".equalsIgnoreCase(uri.getScheme()));
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
