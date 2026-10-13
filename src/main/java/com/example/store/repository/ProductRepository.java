package com.example.store.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.example.store.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = "orders")
    @Query("SELECT p FROM Product p")
    List<Product> findAllWithOrders();

    @EntityGraph(attributePaths = "orders")
    Optional<Product> findWithOrdersById(Long id);

}
