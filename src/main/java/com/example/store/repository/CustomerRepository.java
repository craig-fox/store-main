package com.example.store.repository;

import com.example.store.entity.Customer;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    @EntityGraph(attributePaths = "orders")
    List<Customer> findByNameContainingIgnoreCase(String subString);

    @EntityGraph(attributePaths = "orders")
    @Query("SELECT c FROM Customer c")
    List<Customer> findAllWithOrders();

}
