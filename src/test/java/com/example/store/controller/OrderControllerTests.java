
package com.example.store.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import com.example.store.dto.OrderCustomerDTO;
import com.example.store.dto.OrderDTO;
import com.example.store.dto.OrderProductDto;
import com.example.store.dto.OrderRequestDto;
import com.example.store.exception.OrderNotFoundException;
import com.example.store.service.OrderService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrderController.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @Test
    void testCreateOrder() throws Exception {
        OrderRequestDto request =
                new OrderRequestDto(1L, List.of(10L, 11L));

        when(orderService.createOrder(any(OrderRequestDto.class)))
                .thenReturn(orderDto());

        mockMvc.perform(post("/order")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.name").value("John Doe"))
                .andExpect(jsonPath("$.products.length()").value(2))
                .andExpect(jsonPath("$.products[0].description").value("Widget"));
    }

    @Test
    void testGetOrder() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of(orderDto()));

        mockMvc.perform(get("/order"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Test Order"))
                .andExpect(jsonPath("$[0].customer.name").value("John Doe"))
                .andExpect(jsonPath("$[0].products.length()").value(2));
    }

    @Test
    void testGetOrderById() throws Exception {
        when(orderService.getOrder(1L)).thenReturn(orderDto());

        mockMvc.perform(get("/order/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Test Order"))
                .andExpect(jsonPath("$.customer.name").value("John Doe"))
                .andExpect(jsonPath("$.products[1].description").value("Gadget"));
    }

    @Test
    void testGetOrderByNonexistentIdThrows404() throws Exception {
        long missingOrderId = 0L;

        when(orderService.getOrder(missingOrderId))
                .thenThrow(new OrderNotFoundException(missingOrderId));

        mockMvc.perform(get("/order/" + missingOrderId))
                .andExpect(status().isNotFound());
    }

    private OrderDTO orderDto() {
        OrderCustomerDTO customer = new OrderCustomerDTO();
        customer.setId(1L);
        customer.setName("John Doe");

        OrderProductDto widget = new OrderProductDto(10L, "Widget");
        OrderProductDto gadget = new OrderProductDto(11L, "Gadget");

        OrderDTO dto = new OrderDTO();
        dto.setId(1L);
        dto.setDescription("Test Order");
        dto.setCustomer(customer);
        dto.setProducts(List.of(widget, gadget));

        return dto;
    }
}
