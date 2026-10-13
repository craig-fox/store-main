package com.example.store.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.store.dto.ProductDto;
import com.example.store.dto.ProductRequestDto;
import com.example.store.entity.Product;
import com.example.store.exception.ProductNotFoundException;
import com.example.store.mapper.ProductMapper;
import com.example.store.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        return productMapper.productsToProductDTOs(productRepository.findAllWithOrders());
    }

    @Transactional(readOnly = true)
    public ProductDto getProduct(Long productId) {
        Product product = productRepository.findWithOrdersById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
        return productMapper.productToProductDto(product);
    }

    @Transactional 
    public ProductDto createProduct(ProductRequestDto productRequestDto) {
        Product product = productRepository
            .save(
                productMapper.productRequestDtoToProduct(productRequestDto)
            );
        return productMapper.productToProductDto(product);
    }

}
