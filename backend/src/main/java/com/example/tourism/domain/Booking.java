package com.example.tourism.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "bookings", uniqueConstraints = {
        @UniqueConstraint(name = "uk_booking_user_request", columnNames = {"user_id", "client_request_id"})
})
public class Booking {

    // 主键 ID，数据库自动生成。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 当前预订关联的旅游路线。
    @ManyToOne
    @JoinColumn(name = "route_id", nullable = false)
    private TravelRoute route;

    @JsonIgnore
    @ManyToOne
    @JoinColumn(name = "user_id")
    private AccountUser user;

    // 游客姓名。
    @Column(nullable = false, length = 60)
    private String travelerName;

    // 游客手机号。
    @Column(nullable = false, length = 30)
    private String phone;

    // 幂等请求号，用于防止分布式重试或用户重复点击导致重复下单。
    @Column(name = "client_request_id", length = 64)
    private String clientRequestId;

    // 出行人数。
    @Column(nullable = false)
    private Integer travelers;

    // 订单总金额 = 单人价格 * 出行人数。
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    // 订单状态。
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    // 订单创建时间。
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        // 新订单保存到数据库之前，如果没有时间，就自动填当前时间。
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        // 如果没有指定状态，默认是待确认。
        if (status == null) {
            status = BookingStatus.PENDING;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TravelRoute getRoute() {
        return route;
    }

    public void setRoute(TravelRoute route) {
        this.route = route;
    }

    public AccountUser getUser() {
        return user;
    }

    public void setUser(AccountUser user) {
        this.user = user;
    }

    public String getTravelerName() {
        return travelerName;
    }

    public void setTravelerName(String travelerName) {
        this.travelerName = travelerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getClientRequestId() {
        return clientRequestId;
    }

    public void setClientRequestId(String clientRequestId) {
        this.clientRequestId = clientRequestId;
    }

    public Integer getTravelers() {
        return travelers;
    }

    public void setTravelers(Integer travelers) {
        this.travelers = travelers;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BookingStatus getStatus() {
        return status;
    }

    public void setStatus(BookingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
