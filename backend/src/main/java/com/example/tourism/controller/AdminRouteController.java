package com.example.tourism.controller;

import java.util.List;
import com.example.tourism.domain.TravelRoute;
import com.example.tourism.dto.ApiResponse;
import com.example.tourism.service.TravelRouteService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/routes")
public class AdminRouteController {

    private final TravelRouteService routeService;

    public AdminRouteController(TravelRouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public ApiResponse<List<TravelRoute>> list() {
        return ApiResponse.ok(routeService.findAll());
    }

    @PostMapping
    public ApiResponse<TravelRoute> create(@RequestBody TravelRoute route) {
        return ApiResponse.ok(routeService.save(route));
    }

    @PutMapping("/{id}")
    public ApiResponse<TravelRoute> update(@PathVariable Long id, @RequestBody TravelRoute route) {
        return ApiResponse.ok(routeService.update(id, route));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        routeService.delete(id);
        return ApiResponse.ok("deleted");
    }
}
