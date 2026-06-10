package com.example.tourism.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class BookingRequest {

    // 前端提交的路线 ID。后端会用这个 ID 查询游客想预订哪条路线。
    @NotNull
    private Long routeId;

    // 游客姓名，不能为空。
    @NotBlank
    private String travelerName;

    // 游客手机号，不能为空。
    @NotBlank
    private String phone;

    // 幂等请求号，前端每次创建订单都生成一个唯一值，避免重复提交。
    @NotBlank
    @Size(max = 64)
    private String requestId;

    // 出行人数，最少 1 人。后端会根据人数扣减路线库存。
    @NotNull
    @Min(1)
    private Integer travelers;

    public Long getRouteId() {
        return routeId;
    }

    public void setRouteId(Long routeId) {
        this.routeId = routeId;
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

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Integer getTravelers() {
        return travelers;
    }

    public void setTravelers(Integer travelers) {
        this.travelers = travelers;
    }
}
