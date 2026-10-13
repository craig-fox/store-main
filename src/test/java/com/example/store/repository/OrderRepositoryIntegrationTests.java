package com.example.store.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.example.store.entity.Customer;
import com.example.store.entity.Order;
import com.example.store.entity.Product;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5433/store_test",
    "spring.datasource.username=admin",
    "spring.datasource.password=admin"
})
class OrderRepositoryIntegrationTests {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired 
    private CustomerRepository customerRepository;

    @Autowired 
    private ProductRepository productRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void createOrderWithProducts() {
        Order savedOrder = persistOrder();
        entityManager.clear();

        Order reloadedOrder = orderRepository.findById(savedOrder.getId())
                .orElseThrow();
        assertEquals("Test Customer", reloadedOrder.getCustomer().getName());
        assertEquals(2, reloadedOrder.getProducts().size());   
    }

    @Test
    void retrieveOrderWithCustomerAndProducts() {
        Order savedOrder = persistOrder();
        entityManager.clear();
        List<Order> orders = orderRepository.findAllWithCustomerAndProducts();
        Order retrievedOrder = orders.stream()
                .filter(order -> order.getId().equals(savedOrder.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals("Test Customer", retrievedOrder.getCustomer().getName());
        assertEquals("Test Order", retrievedOrder.getDescription());
        assertEquals(2, retrievedOrder.getProducts().size());

    }

    private Customer testCustomer() {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        return customer;
    }

    private List<Product> testProducts() {
        Product product1 = new Product();
        product1.setDescription("Product 1");
        Product product2 = new Product();
        product2.setDescription("Product 2");
        return List.of(product1, product2);   
    }

    private Order persistOrder() {
        Customer customer = customerRepository.save(testCustomer());
        List<Product> products = productRepository.saveAll(testProducts());

        Order order = new Order();
        order.setCustomer(customer);
        order.setDescription("Test Order");
        order.setProducts(products);

        return orderRepository.saveAndFlush(order);
    }

}
