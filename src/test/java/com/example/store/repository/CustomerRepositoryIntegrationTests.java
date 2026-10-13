package com.example.store.repository;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import com.example.store.entity.Customer;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:postgresql://localhost:5433/store_test",
    "spring.datasource.username=admin",
    "spring.datasource.password=admin"
})
class CustomerRepositoryIntegrationTests {
    @Autowired
    private CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        customerRepository.saveAll(List.of(
            addCustomer("Zzrobert Qqsmith"),
            addCustomer("Zzrobin Qqhood"),
            addCustomer("Bob Zzrobson"),
            addCustomer("Qqalice Jones")
        ));
        customerRepository.flush();
    }

    @Test
    void matchesSubstringAnywhere() {
        assertThat(customerRepository.findByNameContainingIgnoreCase("zzrob"))
            .extracting(Customer::getName)
            .containsExactlyInAnyOrder("Zzrobert Qqsmith", "Zzrobin Qqhood", "Bob Zzrobson");
    }

    @Test
    void matchesSubstringAnywhereWithUppercaseSubstring() {
        assertThat(customerRepository.findByNameContainingIgnoreCase("ZZROB"))
            .extracting(Customer::getName)
            .containsExactlyInAnyOrder("Zzrobert Qqsmith", "Zzrobin Qqhood", "Bob Zzrobson");
    }

    @Test
    void returnsNoMatches() {
        assertThat(customerRepository.findByNameContainingIgnoreCase("NO-MATCHES")).isEmpty();
    }


    private Customer addCustomer(String name) {
        Customer customer = new Customer();
        customer.setName(name);
        return customer;
    }

}
