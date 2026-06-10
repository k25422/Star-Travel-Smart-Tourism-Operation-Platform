package com.example.tourism.controller;

import java.util.List;
import com.example.tourism.domain.Destination;
import com.example.tourism.dto.ApiResponse;
import com.example.tourism.service.DestinationService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/destinations")
public class AdminDestinationController {

    private final DestinationService destinationService;

    public AdminDestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public ApiResponse<List<Destination>> list() {
        return ApiResponse.ok(destinationService.findAll());
    }

    @PostMapping
    public ApiResponse<Destination> create(@RequestBody Destination destination) {
        return ApiResponse.ok(destinationService.save(destination));
    }

    @PutMapping("/{id}")
    public ApiResponse<Destination> update(@PathVariable Long id, @RequestBody Destination destination) {
        return ApiResponse.ok(destinationService.update(id, destination));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        destinationService.delete(id);
        return ApiResponse.ok("deleted");
    }
}
