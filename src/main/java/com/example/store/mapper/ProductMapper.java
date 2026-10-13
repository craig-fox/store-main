package com.example.store.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.store.dto.ProductDto;
import com.example.store.dto.ProductRequestDto;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(source = "orders", target = "orderIds")
    ProductDto productToProductDto(Product product);

    List<ProductDto> productsToProductDTOs(List<Product> products);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "orders", ignore = true)
    Product productRequestDtoToProduct(ProductRequestDto request);

    default Long orderToId(Order order) {
        return order.getId();
    }

}
