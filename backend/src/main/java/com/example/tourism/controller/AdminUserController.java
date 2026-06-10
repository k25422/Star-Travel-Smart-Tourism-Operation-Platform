package com.example.tourism.controller;

import java.util.List;
import javax.validation.Valid;
import com.example.tourism.domain.AccountUser;
import com.example.tourism.dto.AdminUserRequest;
import com.example.tourism.dto.ApiResponse;
import com.example.tourism.service.AdminUserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public ApiResponse<List<AccountUser>> list() {
        return ApiResponse.ok(adminUserService.findAll());
    }

    @PostMapping
    public ApiResponse<AccountUser> create(@Valid @RequestBody AdminUserRequest request) {
        return ApiResponse.ok(adminUserService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<AccountUser> update(@PathVariable Long id, @Valid @RequestBody AdminUserRequest request) {
        return ApiResponse.ok(adminUserService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        adminUserService.delete(id);
        return ApiResponse.ok("deleted");
    }
}
