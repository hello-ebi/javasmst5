package com.example.attendance.service;

import com.example.attendance.dto.EmployeeCreateRequest;
import com.example.attendance.dto.EmployeeResponse;
import com.example.attendance.dto.EmployeeUpdateRequest;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.User;
import com.example.attendance.exception.BadRequestException;
import com.example.attendance.exception.ConflictException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public EmployeeService(EmployeeRepository employeeRepository,
                           UserRepository userRepository,
                           PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<EmployeeResponse> getAllEmployees() {
        return employeeRepository.findAllByOrderByFullNameAsc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EmployeeResponse> getActiveColleagues(Long currentEmployeeId) {
        return employeeRepository.findByUser_IsActiveTrueOrderByFullNameAsc()
                .stream()
                .filter(emp -> currentEmployeeId == null || !emp.getId().equals(currentEmployeeId))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public EmployeeResponse getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Karyawan tidak ditemukan."));
        return mapToResponse(employee);
    }

    @Transactional
    public EmployeeResponse createEmployee(EmployeeCreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email sudah terdaftar dalam sistem.");
        }
        if (employeeRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new ConflictException("Kode Karyawan (Employee ID) sudah digunakan.");
        }

        String role = (request.getRole() != null && !request.getRole().isBlank()) 
                ? request.getRole().toUpperCase() : "EMPLOYEE";

        User user = new User(
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                role
        );
        userRepository.save(user);

        Employee employee = new Employee(
                user,
                request.getEmployeeId().trim(),
                request.getFullName().trim(),
                request.getPhone(),
                request.getDepartment(),
                request.getPosition()
        );
        Employee saved = employeeRepository.save(employee);

        return mapToResponse(saved);
    }

    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Karyawan tidak ditemukan."));

        User user = employee.getUser();

        if (!user.getEmail().equalsIgnoreCase(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email sudah digunakan oleh akun lain.");
        }

        user.setEmail(request.getEmail());
        if (request.getRole() != null && !request.getRole().isBlank()) {
            user.setRole(request.getRole().toUpperCase());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }
        userRepository.save(user);

        employee.setFullName(request.getFullName().trim());
        employee.setPhone(request.getPhone());
        employee.setDepartment(request.getDepartment());
        employee.setPosition(request.getPosition());
        Employee saved = employeeRepository.save(employee);

        return mapToResponse(saved);
    }

    @Transactional
    public void deactivateEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Karyawan tidak ditemukan."));
        User user = employee.getUser();
        user.setIsActive(false);
        userRepository.save(user);
    }

    public EmployeeResponse mapToResponse(Employee emp) {
        EmployeeResponse res = new EmployeeResponse();
        res.setId(emp.getId());
        res.setUserId(emp.getUser().getId());
        res.setEmployeeId(emp.getEmployeeId());
        res.setFullName(emp.getFullName());
        res.setEmail(emp.getUser().getEmail());
        res.setRole(emp.getUser().getRole());
        res.setPhone(emp.getPhone());
        res.setDepartment(emp.getDepartment());
        res.setPosition(emp.getPosition());
        res.setIsActive(emp.getUser().getIsActive());
        res.setCreatedAt(emp.getCreatedAt());
        return res;
    }
}

