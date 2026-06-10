package com.example.tourism.controller;

import java.util.List;
import com.example.tourism.domain.TravelRoute;
import com.example.tourism.service.TravelRouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/routes")
public class TravelRouteController {

    // 路线业务逻辑对象。
    private final TravelRouteService routeService;

    public TravelRouteController(TravelRouteService routeService) {
        this.routeService = routeService;
    }

    @GetMapping
    public List<TravelRoute> list() {
        // GET /api/routes：查询所有旅游路线。
        return routeService.findAll();
    }
}
