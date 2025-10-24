package service;

import org.example.lab01.exception.ApiException;
import org.example.lab01.model.Employee;
import org.example.lab01.model.Position;
import org.example.lab01.service.ApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ApiServiceTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockResponse;

    private ApiService apiService;

    @BeforeEach
    void setUp() throws Exception {
        apiService = new ApiService();
        injectMockHttpClient(apiService, mockHttpClient);
    }


    private void injectMockHttpClient(ApiService apiService, HttpClient httpClient) throws Exception {
        java.lang.reflect.Field field = ApiService.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        field.set(apiService, httpClient);
    }


    @Test
    @DisplayName("Powinien poprawnie sparsować odpowiedź JSON z API")
    void fetchEmployeesFromApi_validJsonResponse_returnsEmployeeList() throws Exception {
        String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan.kowalski@example.com",
                        "company": {
                            "name": "TechCorp"
                        }
                    },
                    {
                        "name": "Anna Nowak",
                        "email": "anna.nowak@example.com",
                        "company": {
                            "name": "DataCorp"
                        }
                    }
                ]
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        List<Employee> employees = apiService.fetchEmployeesFromApi("https://api.example.com/users");
        assertNotNull(employees);
        assertEquals(2, employees.size());
        Employee firstEmployee = employees.get(0);
        assertEquals("Jan", firstEmployee.getFirstName());
        assertEquals("Kowalski", firstEmployee.getLastName());
        assertEquals("jan.kowalski@example.com", firstEmployee.getEmail());
        assertEquals("TechCorp", firstEmployee.getCompanyName());
        assertEquals(Position.PROGRAMISTA, firstEmployee.getPosition());
        assertEquals(Position.PROGRAMISTA.getBaseSalary(), firstEmployee.getSalary(), 0.01);
        Employee secondEmployee = employees.get(1);
        assertEquals("Anna", secondEmployee.getFirstName());
        assertEquals("Nowak", secondEmployee.getLastName());
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        verify(mockResponse, atLeastOnce()).statusCode();
        verify(mockResponse, times(1)).body();
    }

    @Test
    @DisplayName("Powinien obsłużyć nazwisko z wieloma wyrazami")
    void fetchEmployeesFromApi_multipleLastNames_parsesCorrectly() throws Exception {
        String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski Nowak",
                        "email": "jan@example.com",
                        "company": {
                            "name": "TechCorp"
                        }
                    }
                ]
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        List<Employee> employees = apiService.fetchEmployeesFromApi("https://api.example.com/users");
        assertEquals(1, employees.size());
        assertEquals("Jan", employees.get(0).getFirstName());
        assertEquals("Kowalski Nowak", employees.get(0).getLastName());
    }

    @Test
    @DisplayName("Powinien obsłużyć pojedyncze imię bez nazwiska")
    void fetchEmployeesFromApi_singleName_setsEmptyLastName() throws Exception {
        String jsonResponse = """
                [
                    {
                        "name": "Jan",
                        "email": "jan@example.com",
                        "company": {
                            "name": "TechCorp"
                        }
                    }
                ]
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        List<Employee> employees = apiService.fetchEmployeesFromApi("https://api.example.com/users");
        assertEquals(1, employees.size());
        assertEquals("Jan", employees.get(0).getFirstName());
        assertEquals("", employees.get(0).getLastName());
    }
    @Test
    @DisplayName("Powinien rzucić ApiException dla błędu HTTP 404")
    void fetchEmployeesFromApi_http404Error_throwsApiException() throws Exception {
        when(mockResponse.statusCode()).thenReturn(404);
        when(mockResponse.body()).thenReturn("Not Found");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Błąd HTTP: 404"));
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    @DisplayName("Powinien rzucić ApiException dla błędu HTTP 500")
    void fetchEmployeesFromApi_http500Error_throwsApiException() throws Exception {
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("Internal Server Error");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Błąd HTTP: 500"));
    }

    @Test
    @DisplayName("Powinien rzucić ApiException dla błędu HTTP 401")
    void fetchEmployeesFromApi_http401Error_throwsApiException() throws Exception {
        when(mockResponse.statusCode()).thenReturn(401);
        when(mockResponse.body()).thenReturn("Unauthorized");
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Błąd HTTP: 401"));
    }
    @Test
    @DisplayName("Powinien rzucić ApiException przy błędzie połączenia")
    void fetchEmployeesFromApi_connectionError_throwsApiException() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new IOException("Connection refused"));
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Błąd połączenia z API"));
        assertTrue(exception.getMessage().contains("Connection refused"));
    }

    @Test
    @DisplayName("Powinien rzucić ApiException gdy zapytanie zostało przerwane")
    void fetchEmployeesFromApi_interruptedException_throwsApiException() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenThrow(new InterruptedException("Request interrupted"));
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Zapytanie zostało przerwane"));
    }
    @Test
    @DisplayName("Powinien rzucić ApiException przy niepoprawnym formacie JSON")
    void fetchEmployeesFromApi_invalidJson_throwsApiException() throws Exception {
        String invalidJson = "{ this is not valid json }";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(invalidJson);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Nieprawidłowy format JSON") ||
                   exception.getMessage().contains("Błąd parsowania JSON"));
    }

    @Test
    @DisplayName("Powinien rzucić ApiException gdy brakuje wymaganego pola 'name'")
    void fetchEmployeesFromApi_missingNameField_throwsApiException() throws Exception {
        String jsonResponse = """
                [
                    {
                        "email": "jan@example.com",
                        "company": {
                            "name": "TechCorp"
                        }
                    }
                ]
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Brak wymaganego pola 'name'"));
    }

    @Test
    @DisplayName("Powinien rzucić ApiException gdy brakuje wymaganego pola 'email'")
    void fetchEmployeesFromApi_missingEmailField_throwsApiException() throws Exception {
        String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "company": {
                            "name": "TechCorp"
                        }
                    }
                ]
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Brak wymaganego pola 'email'"));
    }

    @Test
    @DisplayName("Powinien rzucić ApiException gdy brakuje obiektu 'company'")
    void fetchEmployeesFromApi_missingCompanyObject_throwsApiException() throws Exception {
        String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com"
                    }
                ]
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Brak obiektu 'company'"));
    }

    @Test
    @DisplayName("Powinien rzucić ApiException gdy pole 'name' jest null")
    void fetchEmployeesFromApi_nullNameField_throwsApiException() throws Exception {
        String jsonResponse = """
                [
                    {
                        "name": null,
                        "email": "jan@example.com",
                        "company": {
                            "name": "TechCorp"
                        }
                    }
                ]
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Brak wymaganego pola 'name'"));
    }

    @Test
    @DisplayName("Powinien rzucić ApiException gdy pole 'name' jest puste")
    void fetchEmployeesFromApi_emptyNameField_throwsApiException() throws Exception {
        String jsonResponse = """
                [
                    {
                        "name": "",
                        "email": "jan@example.com",
                        "company": {
                            "name": "TechCorp"
                        }
                    }
                ]
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        ApiException exception = assertThrows(ApiException.class, () -> {
            apiService.fetchEmployeesFromApi("https://api.example.com/users");
        });

        assertTrue(exception.getMessage().contains("Puste pole 'name'"));
    }

    @Test
    @DisplayName("Powinien zwrócić pustą listę dla pustej tablicy JSON")
    void fetchEmployeesFromApi_emptyJsonArray_returnsEmptyList() throws Exception {
        String jsonResponse = "[]";

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);
        List<Employee> employees = apiService.fetchEmployeesFromApi("https://api.example.com/users");
        assertNotNull(employees);
        assertTrue(employees.isEmpty());
        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
    @Test
    @DisplayName("Powinien wywołać HttpClient dokładnie raz dla poprawnego żądania")
    void fetchEmployeesFromApi_successfulRequest_callsHttpClientOnce() throws Exception {
        String jsonResponse = """
                [
                    {
                        "name": "Jan Kowalski",
                        "email": "jan@example.com",
                        "company": {
                            "name": "TechCorp"
                        }
                    }
                ]
                """;

        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(jsonResponse);
        when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(mockResponse);

        apiService.fetchEmployeesFromApi("https://api.example.com/users");

        verify(mockHttpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
        verify(mockResponse, atLeastOnce()).statusCode();
        verify(mockResponse, times(1)).body();
    }
}
