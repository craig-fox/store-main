package com.example.store.repository;

import com.example.store.entity.Order;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @EntityGraph(attributePaths = {"customer", "products"})
    @Query("SELECT o FROM Order o")
    List<Order> findAllWithCustomerAndProducts();

    @Override
    @EntityGraph(attributePaths = {"customer", "products"})
    Optional<Order> findById(Long id);
    
}
