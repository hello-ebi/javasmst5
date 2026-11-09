package com.example.attendance.config;

import com.example.attendance.entity.Employee;
import com.example.attendance.entity.OfficeLocation;
import com.example.attendance.entity.User;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.OfficeLocationRepository;
import com.example.attendance.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ensures initial default credentials and office locations exist in any environment.
 * SIMPONI — Created by Deddy Harisman
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final OfficeLocationRepository officeLocationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           EmployeeRepository employeeRepository,
                           OfficeLocationRepository officeLocationRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.officeLocationRepository = officeLocationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Seed Super Admin
        if (!userRepository.existsByEmail("admin@simponi.local")) {
            User admin = new User(
                    "admin@simponi.local",
                    passwordEncoder.encode("AdminPass123!"),
                    "ADMIN"
            );
            userRepository.save(admin);
        }

        // 2. Seed Default Sample Employee
        if (!userRepository.existsByEmail("employee@simponi.local")) {
            User empUser = new User(
                    "employee@simponi.local",
                    passwordEncoder.encode("UserPass123!"),
                    "EMPLOYEE"
            );
            userRepository.save(empUser);

            if (!employeeRepository.existsByEmployeeId("EMP-001")) {
                Employee employee = new Employee(
                        empUser,
                        "EMP-001",
                        "Jane Doe",
                        "081234567890",
                        "Teknologi Informasi",
                        "Software Engineer"
                );
                employeeRepository.save(employee);
            }
        }

        // 3. Seed Colleague for Leave Replacement Testing
        if (!userRepository.existsByEmail("john.smith@simponi.local")) {
            User johnUser = new User(
                    "john.smith@simponi.local",
                    passwordEncoder.encode("UserPass123!"),
                    "EMPLOYEE"
            );
            userRepository.save(johnUser);

            if (!employeeRepository.existsByEmployeeId("EMP-002")) {
                Employee john = new Employee(
                        johnUser,
                        "EMP-002",
                        "John Smith",
                        "081298765432",
                        "Teknologi Informasi",
                        "QA Engineer"
                );
                employeeRepository.save(john);
            }
        }

        // 4. Ensure at least one active office location exists
        if (officeLocationRepository.count() == 0) {
            OfficeLocation defaultOffice = new OfficeLocation(
                    "Kantor Pusat (Headquarters)",
                    -6.2088,
                    106.8456,
                    100,
                    true
            );
            officeLocationRepository.save(defaultOffice);
        }
    }
}

