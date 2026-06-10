package com.example.tourism.controller;

import java.util.List;
import com.example.tourism.domain.Booking;
import com.example.tourism.dto.ApiResponse;
import com.example.tourism.dto.BookingStatusRequest;
import com.example.tourism.service.BookingService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    private final BookingService bookingService;

    public AdminBookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public ApiResponse<List<Booking>> list() {
        return ApiResponse.ok(bookingService.findAll());
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Booking> updateStatus(@PathVariable Long id, @RequestBody BookingStatusRequest request) {
        return ApiResponse.ok(bookingService.updateStatus(id, request.getStatus()));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        bookingService.delete(id);
        return ApiResponse.ok("deleted");
    }
}