package com.example.productservice.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {

    private final ProductService productService = new ProductService();

    @Test
    void addProduct() {
        Product product = new Product("Apple", 1.5);
        Product addedProduct = productService.addProduct(product);
        assertNotNull(addedProduct.getId());
        assertEquals("Apple", addedProduct.getName());
        assertEquals(1.5, addedProduct.getPrice());
    }

    @Test
    void getAllProducts() {
        Product product1 = new Product("Banana", 0.75);
        Product product2 = new Product("Cherry", 3.0);

        productService.addProduct(product1);
        productService.addProduct(product2);

        List<Product> products = productService.getAllProducts();
        assertEquals(2, products.size());
    }

    @Test
    void getProduct() {
        Product product = new Product("Date", 4.5);
        Product addedProduct = productService.addProduct(product);

        Product retrievedProduct = productService.getProduct(addedProduct.getId());
        assertNotNull(retrievedProduct);
        assertEquals("Date", retrievedProduct.getName());
    }

    @Test
    void updateProduct() {
        Product product = new Product("Elderberry", 6.0);
        Product addedProduct = productService.addProduct(product);

        Product updatedProduct = new Product("Fig", 7.5);
        Product updated = productService.updateProduct(addedProduct.getId(), updatedProduct);

        assertNotNull(updated);
        assertEquals("Fig", updated.getName());
    }

    @Test
    void deleteProduct() {
        Product product = new Product("Grape", 2.0);
        Product addedProduct = productService.addProduct(product);

        boolean result = productService.deleteProduct(addedProduct.getId());

        assertFalse(result);
    }

    @Test
    void searchProducts() {
        Product product1 = new Product("Honeydew", 8.5);
        Product product2 = new Product("Ice Cream", 9.0);
        Product product3 = new Product("Jelly", 4.5);

        productService.addProduct(product1);
        productService.addProduct(product2);
        productService.addProduct(product3);

        List<Product> searchResults = productService.searchProducts("ice");
        assertEquals(1, searchResults.size());
    }
}