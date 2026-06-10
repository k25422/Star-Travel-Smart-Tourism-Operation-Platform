package com.example.tourism.dto;

import com.example.tourism.domain.BookingStatus;

public class BookingStatusRequest {

    // 前端传入的新订单状态，例如 PENDING、CONFIRMED、CANCELLED。
    private BookingStatus status;

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }
}