package com.example.tourism.service;

import com.example.tourism.domain.Destination;
import com.example.tourism.repository.DestinationRepository;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class DestinationService {

    private final DestinationRepository destinationRepository;

    public DestinationService(DestinationRepository destinationRepository) {
        this.destinationRepository = destinationRepository;
    }

    @Cacheable(cacheNames = "destinations", key = "'all'")
    public List<Destination> findAll() {
        return destinationRepository.findAll();
    }

    @Cacheable(cacheNames = "destinations", key = "#id")
    public Destination findById(Long id) {
        return destinationRepository.findById(id)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("Destination not found");
                    }
                });
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "destinations", allEntries = true),
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public Destination save(Destination destination) {
        return destinationRepository.save(destination);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "destinations", allEntries = true),
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public Destination update(Long id, Destination request) {
        Destination destination = findById(id);
        destination.setName(request.getName());
        destination.setProvince(request.getProvince());
        destination.setTheme(request.getTheme());
        destination.setDescription(request.getDescription());
        destination.setCoverImage(request.getCoverImage());
        destination.setRating(request.getRating());
        destination.setPopularityScore(request.getPopularityScore());
        return destinationRepository.save(destination);
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "destinations", allEntries = true),
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public void delete(Long id) {
        destinationRepository.delete(findById(id));
    }
}