package com.example.tourism.service;

import java.util.List;
import com.example.tourism.domain.AccountUser;
import com.example.tourism.dto.AdminUserRequest;
import com.example.tourism.repository.AccountUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final AccountUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(AccountUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AccountUser> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public AccountUser create(AdminUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (request.getPassword() == null || request.getPassword().trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }

        AccountUser user = new AccountUser();
        applyCommonFields(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public AccountUser update(Long id, AdminUserRequest request) {
        AccountUser user = findById(id);

        if (!user.getUsername().equals(request.getUsername()) && userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        applyCommonFields(user, request);
        if (request.getPassword() != null && request.getPassword().trim().length() >= 6) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        return userRepository.save(user);
    }

    @Transactional
    public void delete(Long id) {
        AccountUser user = findById(id);
        if ("admin".equalsIgnoreCase(user.getUsername())) {
            throw new IllegalArgumentException("Default admin cannot be deleted");
        }
        userRepository.delete(user);
    }

    private AccountUser findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("User not found");
                    }
                });
    }

    private void applyCommonFields(AccountUser user, AdminUserRequest request) {
        user.setUsername(request.getUsername());
        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setRole(request.getRole());
        user.setEnabled(request.getEnabled());
    }
}
