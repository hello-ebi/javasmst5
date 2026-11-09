package com.example.attendance.dto;

public class UserDto {
    private Long id;
    private String email;
    private String role;
    private String employeeId;
    private String fullName;

    public UserDto() {}

    public UserDto(Long id, String email, String role, String employeeId, String fullName) {
        this.id = id;
        this.email = email;
        this.role = role;
        this.employeeId = employeeId;
        this.fullName = fullName;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
}

