package com.example.tourism.controller;

import com.example.tourism.dto.DashboardStats;
import com.example.tourism.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    // 仪表盘统计业务逻辑对象。
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/stats")
    public DashboardStats stats() {
        // GET /api/dashboard/stats：返回首页统计数据。
        return dashboardService.getStats();
    }
}
