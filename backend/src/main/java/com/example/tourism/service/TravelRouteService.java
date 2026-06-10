package com.example.tourism.service;

import com.example.tourism.domain.Destination;
import com.example.tourism.domain.TravelRoute;
import com.example.tourism.repository.DestinationRepository;
import com.example.tourism.repository.TravelRouteRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class TravelRouteService {

    private final TravelRouteRepository routeRepository;
    private final DestinationRepository destinationRepository;

    public TravelRouteService(TravelRouteRepository routeRepository, DestinationRepository destinationRepository) {
        this.routeRepository = routeRepository;
        this.destinationRepository = destinationRepository;
    }

    @Cacheable(cacheNames = "routes", key = "'all'")
    public List<TravelRoute> findAll() {
        return routeRepository.findAll();
    }

    @Cacheable(cacheNames = "routes", key = "#id")
    public TravelRoute findById(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("Route not found");
                    }
                });
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public TravelRoute save(TravelRoute route) {
        route.setDestination(resolveDestination(route));
        return routeRepository.save(route);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public TravelRoute update(Long id, TravelRoute request) {
        TravelRoute route = findById(id);
        route.setDestination(resolveDestination(request));
        route.setTitle(request.getTitle());
        route.setDurationDays(request.getDurationDays());
        route.setPrice(request.getPrice());
        route.setAvailableSeats(request.getAvailableSeats());
        route.setDepartureCity(request.getDepartureCity());
        route.setDepartureDate(request.getDepartureDate());
        route.setGuideName(request.getGuideName());
        route.setHighlight(request.getHighlight());
        return routeRepository.save(route);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public void delete(Long id) {
        routeRepository.delete(findById(id));
    }

    private Destination resolveDestination(TravelRoute route) {
        if (route.getDestination() == null || route.getDestination().getId() == null) {
            throw new IllegalArgumentException("Destination is required");
        }
        return destinationRepository.findById(route.getDestination().getId())
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("Destination not found");
                    }
                });
    }
}