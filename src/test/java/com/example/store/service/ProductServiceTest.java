package com.example.store.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.store.dto.ProductDto;
import com.example.store.dto.ProductRequestDto;
import com.example.store.entity.Order;
import com.example.store.entity.Product;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private ProductRequestDto productRequestDto;
    private Product product;
    private Product productWithId;
    private Product productWithOrders;
    private Order order;
    private ProductDto productDto;


    @BeforeEach
    void setUp() {
        productRequestDto = new ProductRequestDto("Test Product");
        
        product = new Product();
        product.setDescription("Test Product");

        productWithId = new Product();
        productWithId.setDescription("Test Product");
        productWithId.setId(1L);

        order = new Order();
        order.setCustomer(null);
        order.setDescription("Test Order");
        order.setId(10L);


        productWithOrders = new Product();
        productWithOrders.setDescription("Test Product With Orders");
        productWithOrders.setId(1L);
        productWithOrders.setOrders(List.of(order));

        productDto = new ProductDto();
        productDto.setDescription("Test Product");
        productDto.setId(1L);
        productDto.setOrderIds(List.of(10L));

    }

    
    @Test
    void createProduct() {
        when(productMapper.productRequestDtoToProduct(productRequestDto)).thenReturn(product);
        when(productMapper.productToProductDto(productWithId)).thenReturn(productDto);
        when (productRepository.save(product)).thenReturn(productWithId);


        ProductDto newProductDto = productService.createProduct(productRequestDto);
        verify(productRepository).save(product);
        assertEquals(1L, newProductDto.getId());
        assertEquals("Test Product", newProductDto.getDescription());
    }

    @Test
    void getAllProducts() {
        when(productRepository.findAllWithOrders())
            .thenReturn(List.of(productWithId));
    
        when(productMapper.productsToProductDTOs(List.of(productWithId)))
            .thenReturn(List.of(productDto));

        List<ProductDto> products = productService.getAllProducts();
        assertEquals(1, products.size());
        assertEquals(1L, products.get(0).getId());
        assertEquals("Test Product", products.get(0).getDescription());
    }

    @Test
    void getProduct() {
        when(productRepository.findWithOrdersById(1L))
            .thenReturn(Optional.of(productWithOrders));

        when(productMapper.productToProductDto(productWithOrders))
            .thenReturn(productDto);
        ProductDto foundProduct = productService.getProduct(1L);
        assertEquals(1L, foundProduct.getId());
        assertEquals("Test Product", foundProduct.getDescription());
        assertEquals(List.of(10L), foundProduct.getOrderIds());
    }

    @Test
    void getProductWithMissingId() {
        var missingProductId = 0L;
        when(productRepository.findWithOrdersById(missingProductId))
            .thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, ()-> productService.getProduct(missingProductId));
    }
}
