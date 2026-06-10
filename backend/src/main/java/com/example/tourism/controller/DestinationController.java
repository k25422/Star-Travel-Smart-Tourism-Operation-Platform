package com.example.tourism.controller;

import java.util.List;
import com.example.tourism.domain.Destination;
import com.example.tourism.service.DestinationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/destinations")
public class DestinationController {

    // Controller 调 Service，不直接操作数据库。
    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {
        this.destinationService = destinationService;
    }

    @GetMapping
    public List<Destination> list() {
        // GET /api/destinations：查询所有目的地。
        return destinationService.findAll();
    }
}
