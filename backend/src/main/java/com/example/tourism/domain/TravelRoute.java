package com.example.tourism.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Version;

@Entity
@Table(name = "travel_routes")
public class TravelRoute {

    // 主键 ID，数据库自动生成。
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 多条旅游路线可以属于同一个目的地。
    @ManyToOne
    @JoinColumn(name = "destination_id", nullable = false)
    private Destination destination;

    // 路线标题。
    @Column(nullable = false, length = 100)
    private String title;

    // 行程天数。
    @Column(nullable = false)
    private Integer durationDays;

    // 单人价格。
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    // 当前可预订座位数。
    @Column(nullable = false)
    private Integer availableSeats;

    // 出发城市。
    @Column(nullable = false, length = 50)
    private String departureCity;

    // 出发日期。
    @Column(nullable = false)
    private LocalDate departureDate;

    // 导游名称。
    @Column(nullable = false, length = 60)
    private String guideName;

    // 路线亮点说明。
    @Column(nullable = false, length = 300)
    private String highlight;

    // 乐观锁版本号，配合数据库锁能更清楚地表达并发控制意图。
    @Version
    private Long version;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Destination getDestination() {
        return destination;
    }

    public void setDestination(Destination destination) {
        this.destination = destination;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getDurationDays() {
        return durationDays;
    }

    public void setDurationDays(Integer durationDays) {
        this.durationDays = durationDays;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getDepartureCity() {
        return departureCity;
    }

    public void setDepartureCity(String departureCity) {
        this.departureCity = departureCity;
    }

    public LocalDate getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public String getGuideName() {
        return guideName;
    }

    public void setGuideName(String guideName) {
        this.guideName = guideName;
    }

    public String getHighlight() {
        return highlight;
    }

    public void setHighlight(String highlight) {
        this.highlight = highlight;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}
