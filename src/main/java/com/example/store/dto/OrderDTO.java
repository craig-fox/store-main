package com.example.store.dto;

import java.util.List;

import lombok.Data;

@Data
public class OrderDTO {
    private Long id;
    private String description;
    private OrderCustomerDTO customer;
    private List<OrderProductDto> products;
}
