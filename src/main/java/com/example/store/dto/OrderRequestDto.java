package com.example.store.dto;

import java.util.List;

public record OrderRequestDto(Long customerId, List<Long> productIds) {}
