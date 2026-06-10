package com.example.tourism.dto;

import java.math.BigDecimal;

public class DashboardStats {

    // 仪表盘统计数据。这个类不是数据库表，只是返回给前端的 JSON 结构。
    private long destinationCount;
    private long routeCount;
    private long bookingCount;
    private long availableSeats;
    private BigDecimal revenue;
    private double averageRating;

    public DashboardStats(long destinationCount, long routeCount, long bookingCount, long availableSeats,
                          BigDecimal revenue, double averageRating) {
        this.destinationCount = destinationCount;
        this.routeCount = routeCount;
        this.bookingCount = bookingCount;
        this.availableSeats = availableSeats;
        this.revenue = revenue;
        this.averageRating = averageRating;
    }

    public long getDestinationCount() {
        return destinationCount;
    }

    public long getRouteCount() {
        return routeCount;
    }

    public long getBookingCount() {
        return bookingCount;
    }

    public long getAvailableSeats() {
        return availableSeats;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public double getAverageRating() {
        return averageRating;
    }
}
