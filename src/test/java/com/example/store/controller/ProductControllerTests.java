package com.example.store.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.store.dto.ProductDto;
import com.example.store.dto.ProductRequestDto;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(ProductController.class)
public class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    private ProductRequestDto productRequestDto;
    private ProductDto productDto;
    private ProductDto productDtoNoOrders;
    
    @BeforeEach
    void setUp() {
        productRequestDto = new ProductRequestDto("Test Description");

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setDescription("Test Description");
        productDto.setOrderIds(List.of(1L));

        productDtoNoOrders = new ProductDto();
        productDtoNoOrders.setId(1L);
        productDtoNoOrders.setDescription("No Orders");
        productDtoNoOrders.setOrderIds(List.of());
    }

    @Test
    void testCreateProduct() throws Exception {
        when (productService.createProduct(productRequestDto)).thenReturn(productDto);

        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.description").value("Test Description"));
    }

    @Test
    void testGetAllProducts() throws Exception {
        when(productService.getAllProducts()).thenReturn(List.of(productDto));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Test Description"));
    }

    @Test
    void testGetProductById() throws Exception {
        when(productService.getProduct(1L)).thenReturn(productDto);

        mockMvc.perform(get("/products/" + 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Test Description"))
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void testGetProductByNonexistentIdThrows404() throws Exception {
        var missingProductId = 0L;
        when(productService.getProduct(missingProductId))
            .thenThrow(new ProductNotFoundException(missingProductId));
       
        mockMvc.perform(get("/products/" + missingProductId))
            .andExpect(status().isNotFound())
            .andExpect(result -> {
                ProductNotFoundException exception = assertInstanceOf(
                    ProductNotFoundException.class,
                    result.getResolvedException()
                );
            
                assertEquals(
                    "Product not found with id: " + missingProductId,
                    exception.getMessage()
                );
            });
    }


    @Test
    void testCreateProductWithNoOrders() throws JsonProcessingException, Exception {
        when(productService.createProduct(productRequestDto)).thenReturn(productDtoNoOrders);
      
        mockMvc.perform(post("/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(productRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("No Orders"))
                .andExpect(jsonPath("$.orderIds").isEmpty());
    }

}
