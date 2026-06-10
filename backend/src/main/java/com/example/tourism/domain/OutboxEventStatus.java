package com.example.tourism.domain;

public enum OutboxEventStatus {
    PENDING,
    PUBLISHED,
    FAILED
}
