package com.example.store.repository;

import com.example.store.entity.Order;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
    @EntityGraph(attributePaths = "customer")
    @Query("SELECT o FROM Order o")
    List<Order> findAllWithCustomer();
}
