package com.example.attendance.service;

import com.example.attendance.dto.*;
import com.example.attendance.entity.Employee;
import com.example.attendance.entity.User;
import com.example.attendance.exception.BadRequestException;
import com.example.attendance.exception.ConflictException;
import com.example.attendance.exception.ResourceNotFoundException;
import com.example.attendance.repository.EmployeeRepository;
import com.example.attendance.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       EmployeeRepository employeeRepository,
                       PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.employeeRepository = employeeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(authentication);

        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Pengguna tidak ditemukan."));

        UserDto userDto = buildUserDto(user);
        return new LoginResponse(true, "Login berhasil.", userDto);
    }

    public UserDto getCurrentUserDto() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResourceNotFoundException("Pengguna belum login.");
        }
        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Data pengguna tidak ditemukan."));
        return buildUserDto(user);
    }

    public User getCurrentUserEntity() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResourceNotFoundException("Pengguna belum login.");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("Data pengguna tidak ditemukan."));
    }

    public Employee getCurrentEmployeeEntity() {
        User user = getCurrentUserEntity();
        return employeeRepository.findByUser(user)
                .orElseThrow(() -> new ResourceNotFoundException("Profil karyawan untuk pengguna ini tidak ditemukan."));
    }

    @Transactional
    public ApiResponse changeEmail(ChangeEmailRequest request) {
        User user = getCurrentUserEntity();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Kata sandi saat ini salah.");
        }

        if (user.getEmail().equalsIgnoreCase(request.getEmail())) {
            throw new BadRequestException("Email baru tidak boleh sama dengan email lama.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email tersebut sudah digunakan oleh akun lain.");
        }

        user.setEmail(request.getEmail());
        userRepository.save(user);

        return new ApiResponse(true, "Email berhasil diperbarui.");
    }

    @Transactional
    public ApiResponse changePassword(ChangePasswordRequest request) {
        User user = getCurrentUserEntity();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Kata sandi saat ini salah.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return new ApiResponse(true, "Kata sandi berhasil diubah.");
    }

    private UserDto buildUserDto(User user) {
        Optional<Employee> empOpt = employeeRepository.findByUser(user);
        String employeeId = empOpt.map(Employee::getEmployeeId).orElse("-");
        String fullName = empOpt.map(Employee::getFullName).orElse(user.getEmail());

        return new UserDto(user.getId(), user.getEmail(), user.getRole(), employeeId, fullName);
    }
}

