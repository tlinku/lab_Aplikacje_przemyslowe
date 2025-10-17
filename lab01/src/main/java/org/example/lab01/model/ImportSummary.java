package org.example.lab01.model;

import java.util.List;
import java.util.ArrayList;

public class ImportSummary {
    private int importedCount;
    private List<String> errors;

    public ImportSummary(int importedCount) {
        this.importedCount = importedCount;
        this.errors = new ArrayList<>();
    }

    public ImportSummary(int importedCount, List<String> errors) {
        this.importedCount = importedCount;
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public int getImportedCount() { 
        return importedCount; 
    }
    
    public void setImportedCount(int importedCount) { 
        this.importedCount = importedCount; 
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
    }

    public void addError(String error) {
        this.errors.add(error);
    }
}
