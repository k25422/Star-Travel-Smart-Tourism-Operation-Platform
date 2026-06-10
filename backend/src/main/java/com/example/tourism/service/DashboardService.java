package com.example.tourism.service;

import com.example.tourism.domain.Booking;
import com.example.tourism.domain.BookingStatus;
import com.example.tourism.domain.Destination;
import com.example.tourism.domain.TravelRoute;
import com.example.tourism.dto.DashboardStats;
import com.example.tourism.repository.BookingRepository;
import com.example.tourism.repository.DestinationRepository;
import com.example.tourism.repository.TravelRouteRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {

    private final DestinationRepository destinationRepository;
    private final TravelRouteRepository routeRepository;
    private final BookingRepository bookingRepository;

    public DashboardService(DestinationRepository destinationRepository,
                            TravelRouteRepository routeRepository,
                            BookingRepository bookingRepository) {
        this.destinationRepository = destinationRepository;
        this.routeRepository = routeRepository;
        this.bookingRepository = bookingRepository;
    }

    @Cacheable(cacheNames = "dashboardStats", key = "'summary'")
    public DashboardStats getStats() {
        List<Destination> destinations = destinationRepository.findAll();
        List<TravelRoute> routes = routeRepository.findAll();
        List<Booking> bookings = bookingRepository.findAll();

        BigDecimal revenue = BigDecimal.ZERO;
        for (Booking booking : bookings) {
            if (booking.getStatus() != BookingStatus.CANCELLED) {
                revenue = revenue.add(booking.getTotalAmount());
            }
        }

        long seats = 0;
        for (TravelRoute route : routes) {
            seats += route.getAvailableSeats();
        }

        double ratingTotal = 0.0;
        for (Destination destination : destinations) {
            ratingTotal += destination.getRating();
        }
        double averageRating = destinations.isEmpty() ? 0.0 : ratingTotal / destinations.size();

        return new DashboardStats(
                destinations.size(),
                routes.size(),
                bookings.size(),
                seats,
                revenue,
                averageRating
        );
    }
}