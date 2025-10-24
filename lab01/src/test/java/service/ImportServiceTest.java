package service;

import org.example.lab01.model.Employee;
import org.example.lab01.model.ImportSummary;
import org.example.lab01.model.Position;
import org.example.lab01.service.EmployeeService;
import org.example.lab01.service.ImportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


class ImportServiceTest {

    private ImportService importService;
    private EmployeeService employeeService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeService();
        importService = new ImportService();
        setEmployeeService(importService, employeeService);
    }
    private void setEmployeeService(ImportService importService, EmployeeService employeeService) {
        try {
            java.lang.reflect.Field field = ImportService.class.getDeclaredField("employeeService");
            field.setAccessible(true);
            field.set(importService, employeeService);
        } catch (Exception e) {
            throw new RuntimeException("Nie można wstrzyknąć EmployeeService", e);
        }
    }

    private Path createCsvFile(String fileName, String content) throws IOException {
        Path file = tempDir.resolve(fileName);
        Files.writeString(file, content);
        return file;
    }

    @Test
    @DisplayName("Powinien zaimportować poprawne dane z pliku CSV")
    void importFromCsv_validData_importsSuccessfully() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA,8500
                Anna,Nowak,anna.nowak@example.com,DataCorp,MANAGER,12500
                Piotr,Wiśniewski,piotr.wisniewski@example.com,CloudInc,STAZYSTA,3500
                """;
        Path csvFile = createCsvFile("valid_employees.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(3, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
        assertEquals(3, employeeService.getAllEmployees().size());
        List<Employee> employees = employeeService.getAllEmployees();
        Employee firstEmployee = employees.get(0);
        assertEquals("Jan", firstEmployee.getFirstName());
        assertEquals("Kowalski", firstEmployee.getLastName());
        assertEquals("jan.kowalski@example.com", firstEmployee.getEmail());
        assertEquals(Position.PROGRAMISTA, firstEmployee.getPosition());
        assertEquals(8500, firstEmployee.getSalary(), 0.01);
    }

    @Test
    @DisplayName("Powinien zaimportować dane pomijając pustą linię")
    void importFromCsv_withEmptyLines_skipsEmptyLines() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA,8500
                
                Anna,Nowak,anna.nowak@example.com,DataCorp,MANAGER,12500
                """;
        Path csvFile = createCsvFile("with_empty_lines.csv", csvContent);

        ImportSummary summary = importService.importFromCsv(csvFile.toString());

        assertEquals(2, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Powinien zaimportować dane z białymi znakami")
    void importFromCsv_withWhitespace_trimsWhitespace() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                  Jan  ,  Kowalski  ,  jan.kowalski@example.com  ,  TechCorp  ,  PROGRAMISTA  ,  8500  
                """;
        Path csvFile = createCsvFile("with_whitespace.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());

        assertEquals(1, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
        
        Employee employee = employeeService.getAllEmployees().get(0);
        assertEquals("Jan", employee.getFirstName());
        assertEquals("Kowalski", employee.getLastName());
    }

    @Test
    @DisplayName("Powinien odrzucić pracownika z niepoprawnym stanowiskiem")
    void importFromCsv_invalidPosition_addsErrorAndContinues() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA,8500
                Anna,Nowak,anna.nowak@example.com,DataCorp,INVALID_POSITION,12500
                Piotr,Wiśniewski,piotr.wisniewski@example.com,CloudInc,STAZYSTA,3500
                """;
        Path csvFile = createCsvFile("invalid_position.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());

        assertEquals(2, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("Linia 3"));
        assertTrue(summary.getErrors().get(0).contains("Nieznane stanowisko"));
        assertEquals(2, employeeService.getAllEmployees().size());
    }

    @Test
    @DisplayName("Powinien obsłużyć małe litery w stanowisku")
    void importFromCsv_lowercasePosition_convertsToUppercase() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,programista,8500
                """;
        Path csvFile = createCsvFile("lowercase_position.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(1, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
        assertEquals(Position.PROGRAMISTA, employeeService.getAllEmployees().get(0).getPosition());
    }

    @Test
    @DisplayName("Powinien odrzucić pracownika z ujemnym wynagrodzeniem")
    void importFromCsv_negativeSalary_addsErrorAndContinues() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA,8500
                Anna,Nowak,anna.nowak@example.com,DataCorp,MANAGER,-5000
                Piotr,Wiśniewski,piotr.wisniewski@example.com,CloudInc,STAZYSTA,3500
                """;
        Path csvFile = createCsvFile("negative_salary.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(2, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("Linia 3"));
        assertTrue(summary.getErrors().get(0).contains("Wynagrodzenie musi być dodatnie"));
        assertEquals(2, employeeService.getAllEmployees().size());
    }

    @Test
    @DisplayName("Powinien odrzucić pracownika z wynagrodzeniem zero")
    void importFromCsv_zeroSalary_addsError() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA,0
                """;
        Path csvFile = createCsvFile("zero_salary.csv", csvContent);

        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(0, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("Wynagrodzenie musi być dodatnie"));
    }

    @Test
    @DisplayName("Powinien odrzucić linię z niepoprawną liczbą kolumn")
    void importFromCsv_invalidColumnCount_addsError() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA
                Anna,Nowak,anna.nowak@example.com,DataCorp,MANAGER,12500
                """;
        Path csvFile = createCsvFile("invalid_columns.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(1, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("Linia 2"));
        assertTrue(summary.getErrors().get(0).contains("Nieprawidłowa liczba kolumn"));
    }

    @Test
    @DisplayName("Powinien odrzucić linię z pustymi polami")
    void importFromCsv_emptyFields_addsError() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,,jan.kowalski@example.com,TechCorp,PROGRAMISTA,8500
                Anna,Nowak,anna.nowak@example.com,DataCorp,MANAGER,12500
                """;
        Path csvFile = createCsvFile("empty_fields.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(1, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("Linia 2"));
        assertTrue(summary.getErrors().get(0).contains("Puste pola nie są dozwolone"));
    }

    @Test
    @DisplayName("Powinien odrzucić linię z niepoprawnym formatem wynagrodzenia")
    void importFromCsv_invalidSalaryFormat_addsError() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA,ABC
                """;
        Path csvFile = createCsvFile("invalid_salary_format.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(0, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("Linia 2"));
        assertTrue(summary.getErrors().get(0).contains("Nieprawidłowy format wynagrodzenia"));
    }

    @Test
    @DisplayName("Powinien odrzucić pracownika z duplikatem emaila")
    void importFromCsv_duplicateEmail_addsError() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA,8500
                Anna,Nowak,jan.kowalski@example.com,DataCorp,MANAGER,12500
                """;
        Path csvFile = createCsvFile("duplicate_email.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(1, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("Linia 3"));
        assertTrue(summary.getErrors().get(0).contains("Pracownik z tym emailem już istnieje"));
    }


    @Test
    @DisplayName("Powinien obsłużyć nieistniejący plik")
    void importFromCsv_nonExistentFile_returnsErrorSummary() {
        // Given
        String nonExistentFile = tempDir.resolve("nonexistent.csv").toString();

        // When
        ImportSummary summary = importService.importFromCsv(nonExistentFile);

        // Then
        assertEquals(0, summary.getImportedCount());
        assertEquals(1, summary.getErrors().size());
        assertTrue(summary.getErrors().get(0).contains("Błąd odczytu pliku"));
    }

    @Test
    @DisplayName("ImportSummary powinien zawierać poprawne dane dla mieszanych wyników")
    void importFromCsv_mixedResults_correctSummary() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA,8500
                Anna,Nowak,anna.nowak@example.com,DataCorp,INVALID,12500
                Piotr,Wiśniewski,piotr.wisniewski@example.com,CloudInc,STAZYSTA,-3500
                Maria,Kowalska,maria.kowalska@example.com,TechCorp,MANAGER,13000
                Tomasz,Nowicki,jan.kowalski@example.com,OtherCorp,PREZES,26000
                """;
        Path csvFile = createCsvFile("mixed_results.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(2, summary.getImportedCount(), "Powinno zaimportować 2 poprawnych pracowników");
        assertEquals(3, summary.getErrors().size(), "Powinno być 3 błędy");
        List<String> errors = summary.getErrors();
        assertTrue(errors.stream().anyMatch(e -> e.contains("Linia 3") && e.contains("Nieznane stanowisko")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Linia 4") && e.contains("Wynagrodzenie musi być dodatnie")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Linia 6") && e.contains("Pracownik z tym emailem już istnieje")));
    }

    @Test
    @DisplayName("Powinien zaimportować wszystkie poprawne dane bez błędów")
    void importFromCsv_allValid_noErrors() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                Jan,Kowalski,jan.kowalski@example.com,TechCorp,PROGRAMISTA,8500
                Anna,Nowak,anna.nowak@example.com,DataCorp,MANAGER,12500
                """;
        Path csvFile = createCsvFile("all_valid.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(2, summary.getImportedCount());
        assertNotNull(summary.getErrors());
        assertTrue(summary.getErrors().isEmpty(), "Nie powinno być żadnych błędów");
    }

    @Test
    @DisplayName("Powinien zwrócić pusty ImportSummary dla pustego pliku (tylko nagłówek)")
    void importFromCsv_onlyHeader_emptyImport() throws IOException {
        String csvContent = """
                firstName,lastName,email,company,position,salary
                """;
        Path csvFile = createCsvFile("only_header.csv", csvContent);
        ImportSummary summary = importService.importFromCsv(csvFile.toString());
        assertEquals(0, summary.getImportedCount());
        assertTrue(summary.getErrors().isEmpty());
    }
}
