package com.example.store.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderProductDto;
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

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private OrderMapper orderMapper;

    @InjectMocks
    private OrderService orderService;

   

    @Test
    void getAllOrders() {
        Customer customer = customer(1L);
        Product widget = product(10L, "Widget");
        Product gadget = product(11L, "Gadget");
        Order order1 = order(100L, customer, List.of(widget, gadget));
        Order order2 = order(101L, customer, List.of(widget));
        List<Order> orders = List.of(order1, order2);

        OrderDTO dto1 = orderDto(100L, "Order 100",
                List.of(new OrderProductDto(10L, "Widget"), new OrderProductDto(11L, "Gadget")));
        OrderDTO dto2 = orderDto(101L, "Order 101",
                List.of(new OrderProductDto(10L, "Widget")));
        List<OrderDTO> dtos = List.of(dto1, dto2);

        when(orderRepository.findAllWithCustomerAndProducts()).thenReturn(orders);
        when(orderMapper.ordersToOrderDTOs(orders)).thenReturn(dtos);

        List<OrderDTO> result = orderService.getAllOrders();

        assertThat(result).containsExactly(dto1, dto2);
        assertThat(result.get(0).getProducts())
                .extracting(OrderProductDto::id)
                .containsExactly(10L, 11L);
        assertThat(result.get(1).getProducts())
                .extracting(OrderProductDto::id)
                .containsExactly(10L);
    }


    @Test
    void getOrder() {
        Order order = order(100L, customer(1L), List.of(product(10L, "Widget")));
        OrderDTO dto = orderDto(100L, "Order 100", List.of(new OrderProductDto(10L, "Widget")));

        when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
        when(orderMapper.orderToOrderDTO(order)).thenReturn(dto);

        OrderDTO result = orderService.getOrder(100L);

        assertThat(result).isSameAs(dto);
        assertThat(result.getProducts())
                .extracting(OrderProductDto::id)
                .containsExactly(10L);
    }

    @Test
    void getOrderWithMissingId() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrder(99L))
                .isInstanceOf(OrderNotFoundException.class);

        verifyNoInteractions(orderMapper);
    }



    @Test
    void createOrder() {
        Customer customer = customer(1L);
        Product widget = product(10L, "Widget");
        Product gadget = product(11L, "Gadget");
        OrderRequestDto request = request(1L, List.of(10L, 11L));

        Order unsavedOrder = new Order();
        Order savedOrder = order(100L, customer, List.of(widget, gadget));
        OrderDTO dto = orderDto(100L, "Order 100",
                List.of(new OrderProductDto(10L, "Widget"), new OrderProductDto(11L, "Gadget")));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(widget));
        when(productRepository.findById(11L)).thenReturn(Optional.of(gadget));
        when(orderMapper.orderRequestDtoToOrder(request)).thenReturn(unsavedOrder);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.orderToOrderDTO(savedOrder)).thenReturn(dto);

        OrderDTO result = orderService.createOrder(request);

        // The order handed to save() must have the resolved customer and products
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(captor.capture());
        Order orderPassedToSave = captor.getValue();
        assertThat(orderPassedToSave.getCustomer()).isSameAs(customer);
        assertThat(orderPassedToSave.getProducts()).containsExactly(widget, gadget);

        // The returned DTO is the mapped, saved order
        assertThat(result).isSameAs(dto);
    }

    @Test
    void createOrderWithMissingCustomer() {
        OrderRequestDto request = request(99L, List.of(10L));

        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(CustomerNotFoundException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrderWithMissingProduct() {
        Customer customer = customer(1L);
        Product widget = product(10L, "Widget");
        OrderRequestDto request = request(1L, List.of(10L, 99L));

        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(productRepository.findById(10L)).thenReturn(Optional.of(widget));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(request))
                .isInstanceOf(ProductNotFoundException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }



    private static Customer customer(Long id) {
        Customer customer = new Customer();
        customer.setId(id);
        return customer;
    }

    private static Product product(Long id, String description) {
        Product product = new Product();
        product.setId(id);
        product.setDescription(description);
        return product;
    }

    private static Order order(Long id, Customer customer, List<Product> products) {
        Order order = new Order();
        order.setId(id);
        order.setCustomer(customer);
        order.setProducts(new ArrayList<>(products));
        return order;
    }

    private static OrderDTO orderDto(Long id, String description, List<OrderProductDto> products) {
        OrderDTO dto = new OrderDTO();
        dto.setId(id);
        dto.setDescription(description);
        dto.setProducts(products);
        return dto;
    }

    private static OrderRequestDto request(Long customerId, List<Long> productIds) {
        return new OrderRequestDto(customerId, productIds);
    }
}
