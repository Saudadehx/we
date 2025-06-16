package com.example.student_management_system.dto;

public class DashboardStatsDTO {
    private long totalStudents;
    private long totalClasses;
    private long totalGrades;
    private long totalMajors;
    // Getters and Setters
    public long getTotalStudents() { return totalStudents; }
    public void setTotalStudents(long totalStudents) { this.totalStudents = totalStudents; }
    public long getTotalClasses() { return totalClasses; }
    public void setTotalClasses(long totalClasses) { this.totalClasses = totalClasses; }
    public long getTotalGrades() { return totalGrades; }
    public void setTotalGrades(long totalGrades) { this.totalGrades = totalGrades; }
    public long getTotalMajors() { return totalMajors; }
    public void setTotalMajors(long totalMajors) { this.totalMajors = totalMajors; }
}