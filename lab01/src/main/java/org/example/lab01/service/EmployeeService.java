package org.example.lab01.service;

import org.example.lab01.model.Employee;
import org.example.lab01.model.Position;

import java.util.*;
import java.util.stream.Collectors;

public class EmployeeService {

    private final List<Employee> employees = new ArrayList<>();

    public boolean addEmployee(Employee employee) {
        boolean emailExists = employees.stream().anyMatch(e -> e.getEmail().equals(employee.getEmail()));
        if (emailExists) {
            return false;
        }
        employees.add(employee);
        return true;
    }

    public List<Employee> getAllEmployees() {
        return employees;
    }

    public List<Employee> findByCompany(String companyName) {
        return employees.stream().filter(e -> e.getCompanyName().equals(companyName)).toList();
    }

    public List<Employee> sortByLastName() {
        return employees.stream().sorted(Comparator.comparing(Employee::getLastName)).toList();
    }

    public Map<Position, List<Employee>> groupByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getPosition));
    }

    public Map<Position, Long> countByPosition() {
        return employees.stream()
                .collect(Collectors.groupingBy(Employee::getPosition, Collectors.counting()));
    }

    public double getAverageSalary() {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    public Optional<Employee> getHighestPaidEmployee() {
        return employees.stream().max(Comparator.comparing(Employee::getSalary));
    }
}
