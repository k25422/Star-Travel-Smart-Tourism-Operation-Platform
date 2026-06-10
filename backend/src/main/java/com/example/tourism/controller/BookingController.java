package com.example.tourism.controller;

import java.util.List;
import javax.validation.Valid;
import com.example.tourism.domain.Booking;
import com.example.tourism.dto.BookingRequest;
import com.example.tourism.service.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    // Controller 只负责接收前端请求，不直接写复杂业务逻辑。
    // 真正的订单创建、库存扣减逻辑交给 BookingService。
    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping
    public List<Booking> list(Authentication authentication) {
        return bookingService.findByUsername(authentication.getName());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Booking create(@Valid @RequestBody BookingRequest request, Authentication authentication) {
        return bookingService.createBooking(request, authentication.getName());
    }

    @PatchMapping("/{id}/cancel")
    public Booking cancel(@PathVariable Long id, Authentication authentication) {
        return bookingService.cancelOwnBooking(id, authentication.getName());
    }
}
