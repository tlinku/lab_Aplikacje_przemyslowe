package org.example.lab01.model;

public class CompanyStatistics {
    private long employeeCount;
    private double averageSalary;
    private String highestPaidEmployeeName;

    public CompanyStatistics(long employeeCount, double averageSalary, String highestPaidEmployeeName) {
        this.employeeCount = employeeCount;
        this.averageSalary = averageSalary;
        this.highestPaidEmployeeName = highestPaidEmployeeName;
    }

    public long getEmployeeCount() {
        return employeeCount;
    }

    public void setEmployeeCount(long employeeCount) {
        this.employeeCount = employeeCount;
    }

    public double getAverageSalary() {
        return averageSalary;
    }

    public void setAverageSalary(double averageSalary) {
        this.averageSalary = averageSalary;
    }

    public String getHighestPaidEmployeeName() {
        return highestPaidEmployeeName;
    }

    public void setHighestPaidEmployeeName(String highestPaidEmployeeName) {
        this.highestPaidEmployeeName = highestPaidEmployeeName;
    }

    @Override
    public String toString() {
        return String.format("CompanyStatistics{employeeCount=%d, averageSalary=%.2f, highestPaidEmployee='%s'}",
                employeeCount, averageSalary, highestPaidEmployeeName);
    }
}
