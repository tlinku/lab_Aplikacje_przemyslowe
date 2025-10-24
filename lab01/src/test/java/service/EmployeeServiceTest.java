package service;

import org.example.lab01.model.CompanyStatistics;
import org.example.lab01.model.Employee;
import org.example.lab01.model.Position;
import org.example.lab01.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
class EmployeeServiceTest {

    private EmployeeService employeeService;
    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
    }

    @Test
    @DisplayName("Powinien dodać pracownika gdy wszystkie dane są poprawne")
    void addEmployee_validEmployee_returnsTrue() {
        Employee employee = new Employee(
                "Jan", "Kowalski", "jan.kowalski@example.com",
                "TechCorp", Position.PROGRAMISTA, 8500
        );
        boolean result = employeeService.addEmployee(employee);
        assertTrue(result);
        assertEquals(1, employeeService.getAllEmployees().size());
    }

    @Test
    @DisplayName("Nie powinien dodać pracownika gdy email już istnieje")
    void addEmployee_duplicateEmail_returnsFalse() {
        Employee employee1 = new Employee(
                "Jan", "Kowalski", "jan.kowalski@example.com",
                "TechCorp", Position.PROGRAMISTA, 8500
        );
        Employee employee2 = new Employee(
                "Anna", "Nowak", "jan.kowalski@example.com",
                "OtherCorp", Position.MANAGER, 12000
        );
        employeeService.addEmployee(employee1);

        // When
        boolean result = employeeService.addEmployee(employee2);

        // Then
        assertFalse(result);
        assertEquals(1, employeeService.getAllEmployees().size());
    }

    @Test
    @DisplayName("Powinien obsłużyć sytuację gdy pracownik jest null")
    void addEmployee_nullEmployee_handlesGracefully() {

        try {
            employeeService.addEmployee(null);
        } catch (NullPointerException e) {
            assertNotNull(e);
        }
    }

    @Test
    @DisplayName("Powinien dodać wielu pracowników z różnymi emailami")
    void addEmployee_multipleEmployees_allAdded() {
        Employee employee1 = new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8500);
        Employee employee2 = new Employee("Anna", "Nowak", "anna@example.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Piotr", "Wiśniewski", "piotr@example.com", "DataCorp", Position.STAZYSTA, 3500);
        boolean result1 = employeeService.addEmployee(employee1);
        boolean result2 = employeeService.addEmployee(employee2);
        boolean result3 = employeeService.addEmployee(employee3);

        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
        assertEquals(3, employeeService.getAllEmployees().size());
    }
    @Test
    @DisplayName("Powinien znaleźć pracowników z konkretnej firmy")
    void findByCompany_existingCompany_returnsEmployees() {
        Employee employee1 = new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8500);
        Employee employee2 = new Employee("Anna", "Nowak", "anna@example.com", "TechCorp", Position.MANAGER, 12000);
        Employee employee3 = new Employee("Piotr", "Wiśniewski", "piotr@example.com", "DataCorp", Position.STAZYSTA, 3500);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);
        List<Employee> techCorpEmployees = employeeService.findByCompany("TechCorp");
        assertEquals(2, techCorpEmployees.size());
        assertTrue(techCorpEmployees.stream().allMatch(e -> e.getCompanyName().equals("TechCorp")));
    }

    @Test
    @DisplayName("Powinien zwrócić pustą listę gdy firma nie istnieje")
    void findByCompany_nonExistingCompany_returnsEmptyList() {
        Employee employee = new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8500);
        employeeService.addEmployee(employee);
        List<Employee> result = employeeService.findByCompany("NonExistingCorp");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Powinien zwrócić pustą listę gdy baza jest pusta")
    void findByCompany_emptyDatabase_returnsEmptyList() {
        List<Employee> result = employeeService.findByCompany("AnyCompany");
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Powinien obliczyć średnie wynagrodzenie poprawnie")
    void getAverageSalary_withEmployees_returnsCorrectAverage() {
        employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000));
        employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@example.com", "TechCorp", Position.MANAGER, 12000));
        employeeService.addEmployee(new Employee("Piotr", "Wiśniewski", "piotr@example.com", "DataCorp", Position.STAZYSTA, 4000));
        double average = employeeService.getAverageSalary();
        assertEquals(8000.0, average, 0.01);
    }

    @Test
    @DisplayName("Powinien zwrócić 0.0 gdy baza jest pusta")
    void getAverageSalary_emptyDatabase_returnsZero() {
        double average = employeeService.getAverageSalary();
        assertEquals(0.0, average, 0.01);
    }

    @Test
    @DisplayName("Powinien obliczyć średnie dla jednego pracownika")
    void getAverageSalary_singleEmployee_returnsSalary() {
        employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8500));
        double average = employeeService.getAverageSalary();
        assertEquals(8500.0, average, 0.01);
    }
    @Test
    @DisplayName("Powinien zwrócić najlepiej zarabiającego pracownika")
    void getHighestPaidEmployee_withEmployees_returnsHighestPaid() {
        Employee employee1 = new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000);
        Employee employee2 = new Employee("Anna", "Nowak", "anna@example.com", "TechCorp", Position.MANAGER, 15000);
        Employee employee3 = new Employee("Piotr", "Wiśniewski", "piotr@example.com", "DataCorp", Position.STAZYSTA, 4000);
        employeeService.addEmployee(employee1);
        employeeService.addEmployee(employee2);
        employeeService.addEmployee(employee3);
        Optional<Employee> highestPaid = employeeService.getHighestPaidEmployee();
        assertTrue(highestPaid.isPresent());
        assertEquals("Anna", highestPaid.get().getFirstName());
        assertEquals(15000, highestPaid.get().getSalary(), 0.01);
    }

    @Test
    @DisplayName("Powinien zwrócić Optional.empty() gdy baza jest pusta")
    void getHighestPaidEmployee_emptyDatabase_returnsEmptyOptional() {
        Optional<Employee> result = employeeService.getHighestPaidEmployee();
        assertFalse(result.isPresent());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Powinien zwrócić pracownika gdy jest tylko jeden")
    void getHighestPaidEmployee_singleEmployee_returnsEmployee() {
        Employee employee = new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8500);
        employeeService.addEmployee(employee);
        Optional<Employee> result = employeeService.getHighestPaidEmployee();
        assertTrue(result.isPresent());
        assertEquals("Jan", result.get().getFirstName());
    }
    @Test
    @DisplayName("Powinien znaleźć pracowników z wynagrodzeniem niższym niż bazowe")
    void validateSalaryConsistency_withInconsistencies_returnsInconsistentEmployees() {
        Employee consistent = new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 9000);
        Employee inconsistent1 = new Employee("Anna", "Nowak", "anna@example.com", "TechCorp", Position.MANAGER, 10000); // Bazowe: 12000
        Employee inconsistent2 = new Employee("Piotr", "Wiśniewski", "piotr@example.com", "DataCorp", Position.PREZES, 20000); // Bazowe: 25000
        employeeService.addEmployee(consistent);
        employeeService.addEmployee(inconsistent1);
        employeeService.addEmployee(inconsistent2);
        List<Employee> inconsistencies = employeeService.validateSalaryConsistency();
        assertEquals(2, inconsistencies.size());
        assertTrue(inconsistencies.stream().anyMatch(e -> e.getEmail().equals("anna@example.com")));
        assertTrue(inconsistencies.stream().anyMatch(e -> e.getEmail().equals("piotr@example.com")));
    }

    @Test
    @DisplayName("Powinien zwrócić pustą listę gdy wszystkie wynagrodzenia są poprawne")
    void validateSalaryConsistency_allConsistent_returnsEmptyList() {
        employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 9000));
        employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@example.com", "TechCorp", Position.MANAGER, 13000));
        List<Employee> inconsistencies = employeeService.validateSalaryConsistency();
        assertTrue(inconsistencies.isEmpty());
    }

    @Test
    @DisplayName("Powinien zwrócić pustą listę gdy baza jest pusta")
    void validateSalaryConsistency_emptyDatabase_returnsEmptyList() {
        List<Employee> result = employeeService.validateSalaryConsistency();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Powinien zgrupować pracowników według stanowisk")
    void groupByPosition_withEmployees_groupsCorrectly() {
        employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000));
        employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@example.com", "TechCorp", Position.PROGRAMISTA, 8500));
        employeeService.addEmployee(new Employee("Piotr", "Wiśniewski", "piotr@example.com", "DataCorp", Position.MANAGER, 12000));
        Map<Position, List<Employee>> grouped = employeeService.groupByPosition();
        assertEquals(2, grouped.size());
        assertEquals(2, grouped.get(Position.PROGRAMISTA).size());
        assertEquals(1, grouped.get(Position.MANAGER).size());
    }
    @Test
    @DisplayName("Powinien policzyć pracowników według stanowisk")
    void countByPosition_withEmployees_countsCorrectly() {
        employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000));
        employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@example.com", "TechCorp", Position.PROGRAMISTA, 8500));
        employeeService.addEmployee(new Employee("Piotr", "Wiśniewski", "piotr@example.com", "DataCorp", Position.MANAGER, 12000));
        Map<Position, Long> counts = employeeService.countByPosition();
        assertEquals(2, counts.size());
        assertEquals(2L, counts.get(Position.PROGRAMISTA));
        assertEquals(1L, counts.get(Position.MANAGER));
    }

    @Test
    @DisplayName("Powinien zwrócić statystyki dla firm")
    void getCompanyStatistics_withEmployees_returnsCorrectStatistics() {
        // Given
        employeeService.addEmployee(new Employee("Jan", "Kowalski", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000));
        employeeService.addEmployee(new Employee("Anna", "Nowak", "anna@example.com", "TechCorp", Position.MANAGER, 12000));
        employeeService.addEmployee(new Employee("Piotr", "Wiśniewski", "piotr@example.com", "DataCorp", Position.STAZYSTA, 4000));

        // When
        Map<String, CompanyStatistics> stats = employeeService.getCompanyStatistics();

        // Then
        assertEquals(2, stats.size());
        
        CompanyStatistics techCorpStats = stats.get("TechCorp");
        assertNotNull(techCorpStats);
        assertEquals(2, techCorpStats.getEmployeeCount());
        assertEquals(10000.0, techCorpStats.getAverageSalary(), 0.01);
        assertEquals("Anna Nowak", techCorpStats.getHighestPaidEmployeeName());

        CompanyStatistics dataCorpStats = stats.get("DataCorp");
        assertNotNull(dataCorpStats);
        assertEquals(1, dataCorpStats.getEmployeeCount());
        assertEquals(4000.0, dataCorpStats.getAverageSalary(), 0.01);
    }
    @Test
    @DisplayName("Powinien posortować pracowników według nazwiska")
    void sortByLastName_withEmployees_sortsCorrectly() {
        employeeService.addEmployee(new Employee("Jan", "Zieliński", "jan@example.com", "TechCorp", Position.PROGRAMISTA, 8000));
        employeeService.addEmployee(new Employee("Anna", "Kowalska", "anna@example.com", "TechCorp", Position.MANAGER, 12000));
        employeeService.addEmployee(new Employee("Piotr", "Nowak", "piotr@example.com", "DataCorp", Position.STAZYSTA, 4000));
        List<Employee> sorted = employeeService.sortByLastName();
        assertEquals(3, sorted.size());
        assertEquals("Kowalska", sorted.get(0).getLastName());
        assertEquals("Nowak", sorted.get(1).getLastName());
        assertEquals("Zieliński", sorted.get(2).getLastName());
    }
}
