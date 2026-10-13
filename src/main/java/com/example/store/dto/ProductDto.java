package com.example.store.dto;

import java.util.List;

import lombok.Data;

@Data
public class ProductDto {
    private Long id;
    private String description;
    private List<Long> orderIds;
}
