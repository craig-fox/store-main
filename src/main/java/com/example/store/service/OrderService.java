package com.example.store.service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderRequestDto;
import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exception.CustomerNotFoundException;
import com.example.store.exception.OrderNotFoundException;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.mapper.OrderMapper;
import com.example.store.repository.CustomerRepository;
import com.example.store.repository.OrderRepository;
import com.example.store.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class OrderService {
    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        return orderMapper.ordersToOrderDTOs(orderRepository.findAllWithCustomerAndProducts());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
        return orderMapper.orderToOrderDTO(order);
    }

    @Transactional 
    public OrderDTO createOrder(OrderRequestDto orderRequestDto) {
        Customer customer = customerRepository.findById(orderRequestDto.customerId())
            .orElseThrow(() -> new CustomerNotFoundException(orderRequestDto.customerId()));
        List<Product> products = new ArrayList<>();
        for (Long productId : orderRequestDto.productIds()) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));
            products.add(product);
        }

        Order order = orderMapper.orderRequestDtoToOrder(orderRequestDto);
        order.setDescription("Order for customer " + customer.getName());
        order.setCustomer(customer);
        order.setProducts(products);
        return orderMapper.orderToOrderDTO(orderRepository.save(order));
    }
}
