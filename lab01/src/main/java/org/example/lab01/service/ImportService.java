package org.example.lab01.service;

import org.example.lab01.model.Employee;
import org.example.lab01.model.ImportSummary;
import org.example.lab01.model.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class ImportService {

    @Autowired
    private EmployeeService employeeService;

    public ImportSummary importFromCsv(String filePath) {
        List<String> errors = new ArrayList<>();
        int importedCount = 0;
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isFirstLine = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                if (line.trim().isEmpty()) {
                    continue;
                }

                try {
                    Employee employee = parseEmployeeFromCsvLine(line, lineNumber);
                    if (employee != null) {
                        boolean added = employeeService.addEmployee(employee);
                        if (added) {
                            importedCount++;
                        } else {
                            errors.add("Linia " + lineNumber + ": Pracownik z tym emailem już istnieje");
                        }
                    }
                } catch (Exception e) {
                    errors.add("Linia " + lineNumber + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            errors.add("Błąd odczytu pliku: " + e.getMessage());
        }

        return new ImportSummary(importedCount, errors);
    }

    private Employee parseEmployeeFromCsvLine(String line, int lineNumber) throws Exception {
        String[] fields = line.split(",");
        
        if (fields.length != 6) {
            throw new Exception("Nieprawidłowa liczba kolumn (oczekiwano 6, otrzymano " + fields.length + ")");
        }

        for (int i = 0; i < fields.length; i++) {
            fields[i] = fields[i].trim();
        }

        String firstName = fields[0];
        String lastName = fields[1];
        String email = fields[2];
        String company = fields[3];
        String positionStr = fields[4];
        String salaryStr = fields[5];

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() ||
            company.isEmpty() || positionStr.isEmpty() || salaryStr.isEmpty()) {
            throw new Exception("Puste pola nie są dozwolone");
        }

        Position position;
        try {
            position = Position.valueOf(positionStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new Exception("Nieznane stanowisko: " + positionStr);
        }

        double salary;
        try {
            salary = Double.parseDouble(salaryStr);
        } catch (NumberFormatException e) {
            throw new Exception("Nieprawidłowy format wynagrodzenia: " + salaryStr);
        }

        if (salary <= 0) {
            throw new Exception("Wynagrodzenie musi być dodatnie, otrzymano: " + salary);
        }

        return new Employee(firstName, lastName, email, company, position, salary);
    }

    public ImportSummary importData() {
        return new ImportSummary(0);
    }
}
