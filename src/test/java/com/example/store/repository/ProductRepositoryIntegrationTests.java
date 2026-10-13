
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
class ProductRepositoryIntegrationTests {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void createAndReloadProduct() {
        Product product = new Product();
        product.setDescription("Test Product");

        Product savedProduct = productRepository.saveAndFlush(product);
        Long productId = savedProduct.getId();

        entityManager.clear();

        Product reloadedProduct = productRepository.findById(productId)
                .orElseThrow();

        assertEquals(productId, reloadedProduct.getId());
        assertEquals("Test Product", reloadedProduct.getDescription());
    }

    @Test
    void retrieveProductWithOrders() {
        Product product = productRepository.saveAndFlush(testProduct());
        Customer customer = customerRepository.saveAndFlush(testCustomer());

        Order firstOrder = orderRepository.saveAndFlush(
                testOrder(customer, "First Order", List.of(product)));
        Order secondOrder = orderRepository.saveAndFlush(
                testOrder(customer, "Second Order", List.of(product)));

        entityManager.clear();

        Product retrievedProduct = productRepository
                .findWithOrdersById(product.getId())
                .orElseThrow();

        assertEquals(
                List.of(firstOrder.getId(), secondOrder.getId()),
                retrievedProduct.getOrders().stream()
                        .map(Order::getId)
                        .sorted()
                        .toList());
    }

    @Test
    void retrieveAllProductsWithOrders() {
        Product product = productRepository.saveAndFlush(testProduct());
        Customer customer = customerRepository.saveAndFlush(testCustomer());

        Order order = orderRepository.saveAndFlush(
                testOrder(customer, "Test Order", List.of(product)));

        entityManager.clear();

        List<Product> products = productRepository.findAllWithOrders();

        Product retrievedProduct = products.stream()
                .filter(p -> p.getId().equals(product.getId()))
                .findFirst()
                .orElseThrow();

        assertEquals(
                List.of(order.getId()),
                retrievedProduct.getOrders().stream()
                        .map(Order::getId)
                        .toList());
    }

    private Product testProduct() {
        Product product = new Product();
        product.setDescription("Test Product");
        return product;
    }

    private Customer testCustomer() {
        Customer customer = new Customer();
        customer.setName("Test Customer");
        return customer;
    }

    private Order testOrder(
            Customer customer, String description, List<Product> products) {
        Order order = new Order();
        order.setCustomer(customer);
        order.setDescription(description);
        order.setProducts(products);
        return order;
    }
}

