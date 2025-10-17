package org.example.lab01.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import exception.ApiException;
import org.example.lab01.model.Employee;
import org.example.lab01.model.Position;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class ApiService {

    private final HttpClient httpClient;
    private final Gson gson;

    public ApiService() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.gson = new Gson();
    }

    public List<Employee> fetchEmployeesFromApi(String apiUrl) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/json")
                    .timeout(Duration.ofSeconds(30))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ApiException("Błąd HTTP: " + response.statusCode() + " - " + response.body());
            }
            return parseEmployeesFromJson(response.body());

        } catch (IOException e) {
            throw new ApiException("Błąd połączenia z API: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException("Zapytanie zostało przerwane: " + e.getMessage());
        } catch (JsonSyntaxException e) {
            throw new ApiException("Błąd parsowania JSON: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiException("Nieoczekiwany błąd: " + e.getMessage());
        }
    }

    private List<Employee> parseEmployeesFromJson(String jsonResponse) {
        try {
            JsonArray jsonArray = gson.fromJson(jsonResponse, JsonArray.class);
            List<Employee> employees = new ArrayList<>();

            for (JsonElement element : jsonArray) {
                JsonObject userObject = element.getAsJsonObject();
                String fullName = getStringField(userObject, "name");
                String email = getStringField(userObject, "email");
                JsonObject companyObject = userObject.getAsJsonObject("company");
                if (companyObject == null) {
                    throw new ApiException("Brak obiektu 'company' w odpowiedzi API");
                }
                String companyName = getStringField(companyObject, "name");
                String[] nameParts = splitFullName(fullName);
                String firstName = nameParts[0];
                String lastName = nameParts[1];
                Position position = Position.PROGRAMISTA;
                double salary = position.getBaseSalary();

                Employee employee = new Employee(firstName, lastName, email, companyName, position, salary);
                employees.add(employee);
            }

            return employees;

        } catch (JsonSyntaxException e) {
            throw new ApiException("Nieprawidłowy format JSON: " + e.getMessage());
        } catch (Exception e) {
            throw new ApiException("Błąd podczas parsowania danych pracowników: " + e.getMessage());
        }
    }

    private String getStringField(JsonObject jsonObject, String fieldName) {
        JsonElement element = jsonObject.get(fieldName);
        if (element == null || element.isJsonNull()) {
            throw new ApiException("Brak wymaganego pola '" + fieldName + "' w odpowiedzi API");
        }
        return element.getAsString();
    }

    private String[] splitFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new ApiException("Puste pole 'name'");
        }

        String[] parts = fullName.trim().split("\\s+", 2);
        if (parts.length == 1) {
            return new String[]{parts[0], ""};
        } else {
            return new String[]{parts[0], parts[1]};
        }
    }
}
