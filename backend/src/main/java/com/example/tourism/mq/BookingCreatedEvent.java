package com.example.tourism.mq;

import java.math.BigDecimal;

public class BookingCreatedEvent {

    private Long bookingId;
    private Long routeId;
    private String routeTitle;
    private String travelerName;
    private String phone;
    private Integer travelers;
    private BigDecimal totalAmount;

    public BookingCreatedEvent() {
    }

    public BookingCreatedEvent(Long bookingId, Long routeId, String routeTitle, String travelerName, String phone, Integer travelers, BigDecimal totalAmount) {
        this.bookingId = bookingId;
        this.routeId = routeId;
        this.routeTitle = routeTitle;
        this.travelerName = travelerName;
        this.phone = phone;
        this.travelers = travelers;
        this.totalAmount = totalAmount;
    }

    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }
    public String getRouteTitle() { return routeTitle; }
    public void setRouteTitle(String routeTitle) { this.routeTitle = routeTitle; }
    public String getTravelerName() { return travelerName; }
    public void setTravelerName(String travelerName) { this.travelerName = travelerName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Integer getTravelers() { return travelers; }
    public void setTravelers(Integer travelers) { this.travelers = travelers; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}