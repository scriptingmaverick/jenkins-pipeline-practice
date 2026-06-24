package com.example.productservice.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProductTest {

    @Test
    public void testProductConstructor() {
        Product product = new Product(1L, "Test Product", 19.99);
        assertEquals(1L, product.getId());
        assertEquals("Test Product", product.getName());
        assertEquals(19.99, product.getPrice(), 0.001);
    }

    @Test
    public void testProductGettersSetters() {
        Product product = new Product();
        product.setId(2L);
        product.setName("Updated Test Product");
        product.setPrice(29.99);

        assertEquals(2L, product.getId());
        assertEquals("Updated Test Product", product.getName());
        assertEquals(29.99, product.getPrice(), 0.001);
    }
}