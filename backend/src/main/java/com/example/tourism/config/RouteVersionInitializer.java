package com.example.tourism.config;

import com.example.tourism.repository.TravelRouteRepository;
import javax.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class RouteVersionInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(RouteVersionInitializer.class);

    private final TravelRouteRepository routeRepository;

    public RouteVersionInitializer(TravelRouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        int updated = routeRepository.initializeMissingVersions();
        if (updated > 0) {
            log.info("Initialized missing route version values: {}", updated);
        }
    }
}
