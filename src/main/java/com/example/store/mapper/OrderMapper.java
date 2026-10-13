package com.example.store.mapper;

import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderProductDto;
import com.example.store.dto.OrderRequestDto;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    OrderDTO orderToOrderDTO(Order order);

    List<OrderDTO> ordersToOrderDTOs(List<Order> orders);

    OrderCustomerDTO orderToOrderCustomerDTO(Customer customer);

    OrderProductDto productToOrderProductDto(Product product);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "products", ignore = true)
    Order orderRequestDtoToOrder(OrderRequestDto request);


}
