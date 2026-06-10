package com.example.tourism.service;

import com.example.tourism.domain.AccountUser;
import com.example.tourism.domain.Booking;
import com.example.tourism.domain.BookingStatus;
import com.example.tourism.domain.TravelRoute;
import com.example.tourism.dto.BookingRequest;
import com.example.tourism.repository.AccountUserRepository;
import com.example.tourism.repository.BookingRepository;
import com.example.tourism.repository.TravelRouteRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final TravelRouteRepository routeRepository;
    private final AccountUserRepository userRepository;
    private final BookingOutboxService bookingOutboxService;

    public BookingService(BookingRepository bookingRepository,
                          TravelRouteRepository routeRepository,
                          AccountUserRepository userRepository,
                          BookingOutboxService bookingOutboxService) {
        this.bookingRepository = bookingRepository;
        this.routeRepository = routeRepository;
        this.userRepository = userRepository;
        this.bookingOutboxService = bookingOutboxService;
    }

    @Cacheable(cacheNames = "bookings", key = "'all'")
    public List<Booking> findAll() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    @Cacheable(cacheNames = "bookings", key = "'user:' + #username")
    public List<Booking> findByUsername(String username) {
        return bookingRepository.findByUserUsernameOrderByCreatedAtDesc(username);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookings", allEntries = true),
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public Booking createBooking(BookingRequest request, String username) {
        Booking existingBooking = bookingRepository.findByUserUsernameAndClientRequestId(username, request.getRequestId()).orElse(null);
        if (existingBooking != null) {
            return existingBooking;
        }

        TravelRoute route = findRouteForUpdate(request.getRouteId());
        AccountUser user = findUser(username);

        if (route.getAvailableSeats() < request.getTravelers()) {
            throw new IllegalArgumentException("Not enough seats for this route");
        }

        route.setAvailableSeats(route.getAvailableSeats() - request.getTravelers());

        Booking booking = new Booking();
        booking.setRoute(route);
        booking.setUser(user);
        booking.setTravelerName(request.getTravelerName());
        booking.setPhone(request.getPhone());
        booking.setClientRequestId(request.getRequestId());
        booking.setTravelers(request.getTravelers());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setTotalAmount(route.getPrice().multiply(BigDecimal.valueOf(request.getTravelers())));

        try {
            Booking savedBooking = bookingRepository.saveAndFlush(booking);
            bookingOutboxService.enqueueBookingCreated(savedBooking);
            return savedBooking;
        } catch (DataIntegrityViolationException ex) {
            Booking duplicatedBooking = bookingRepository.findByUserUsernameAndClientRequestId(username, request.getRequestId()).orElse(null);
            if (duplicatedBooking != null) {
                return duplicatedBooking;
            }
            throw ex;
        }
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookings", allEntries = true),
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public Booking updateStatus(Long id, BookingStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("Booking status is required");
        }

        Booking booking = findBooking(id);
        return changeStatus(booking, status);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookings", allEntries = true),
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public Booking cancelOwnBooking(Long id, String username) {
        Booking booking = findBookingForUser(id, username);
        return changeStatus(booking, BookingStatus.CANCELLED);
    }

    private Booking changeStatus(Booking booking, BookingStatus status) {
        BookingStatus currentStatus = booking.getStatus();

        if (currentStatus == status) {
            return booking;
        }

        if (currentStatus != BookingStatus.CANCELLED && status == BookingStatus.CANCELLED) {
            releaseSeats(booking);
        }
        if (currentStatus == BookingStatus.CANCELLED && status != BookingStatus.CANCELLED) {
            occupySeats(booking);
        }

        booking.setStatus(status);
        return bookingRepository.save(booking);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "bookings", allEntries = true),
            @CacheEvict(cacheNames = "routes", allEntries = true),
            @CacheEvict(cacheNames = "dashboardStats", allEntries = true)
    })
    public void delete(Long id) {
        Booking booking = findBooking(id);

        if (booking.getStatus() != BookingStatus.CANCELLED) {
            releaseSeats(booking);
        }

        bookingRepository.delete(booking);
    }

    private Booking findBooking(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("Booking not found");
                    }
                });
    }

    private Booking findBookingForUser(Long id, String username) {
        return bookingRepository.findByIdAndUserUsername(id, username)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("Booking not found for current user");
                    }
                });
    }

    private TravelRoute findRoute(Long id) {
        return routeRepository.findById(id)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("Route not found");
                    }
                });
    }

    private TravelRoute findRouteForUpdate(Long id) {
        return routeRepository.findByIdForUpdate(id)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("Route not found");
                    }
                });
    }

    private AccountUser findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("User not found");
                    }
                });
    }

    private void occupySeats(Booking booking) {
        TravelRoute route = findRouteForUpdate(booking.getRoute().getId());
        if (route.getAvailableSeats() < booking.getTravelers()) {
            throw new IllegalArgumentException("Not enough seats to reactivate this booking");
        }
        route.setAvailableSeats(route.getAvailableSeats() - booking.getTravelers());
        booking.setRoute(route);
    }

    private void releaseSeats(Booking booking) {
        TravelRoute route = findRouteForUpdate(booking.getRoute().getId());
        route.setAvailableSeats(route.getAvailableSeats() + booking.getTravelers());
        booking.setRoute(route);
    }
}
