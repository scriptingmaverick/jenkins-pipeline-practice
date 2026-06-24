package com.example.productservice.service;

import com.example.productservice.model.Product;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class ProductService {

    private final List<Product> products = new ArrayList<>();
    private final AtomicLong idGenerator = new AtomicLong();

    public Product addProduct(Product product) {
        product.setId(idGenerator.incrementAndGet());
        products.add(product);
        return product;
    }

    public List<Product> getAllProducts() {
        return products;
    }

    public Product getProduct(Long id) {
        return products.stream()
                .filter(product -> product.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Product updateProduct(Long id, Product updatedProduct) {

        Product existingProduct = getProduct(id);

        if (existingProduct == null) {
            return null;
        }

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setPrice(updatedProduct.getPrice());

        return existingProduct;
    }

    public boolean deleteProduct(Long id) {
        return products.removeIf(product -> product.getId().equals(id));
    }

    public List<Product> searchProducts(String name) {

        return products.stream()
                .filter(product ->
                        product.getName()
                                .toLowerCase()
                                .contains(name.toLowerCase()))
                .toList();
    }
}