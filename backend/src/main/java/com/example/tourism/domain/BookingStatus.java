package com.example.tourism.domain;

public enum BookingStatus {
    // 待确认：客服或管理员还没有处理。
    PENDING,

    // 已确认：订单有效，可以进入后续出行准备。
    CONFIRMED,

    // 已取消：订单无效，前端统计时不计入有效收入。
    CANCELLED
}