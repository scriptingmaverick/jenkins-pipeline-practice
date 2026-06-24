package com.example.productservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.productservice.model.Product;
import com.example.productservice.service.ProductService;

@WebMvcTest(ProductController.class)
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    @BeforeEach
    public void setUp() {
        when(productService.addProduct(any(Product.class))).thenReturn(new Product());
        when(productService.getAllProducts()).thenReturn(java.util.Collections.emptyList());
        when(productService.getProduct(anyLong())).thenReturn(new Product());
        when(productService.updateProduct(anyLong(), any(Product.class))).thenReturn(new Product());
        when(productService.deleteProduct(anyLong())).thenReturn(true);
        when(productService.searchProducts(anyString())).thenReturn(java.util.Collections.emptyList());
    }

    @Test
    public void testAddProduct() throws Exception {
        mockMvc.perform(post("/products")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void testGetAllProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    public void testGetProduct() throws Exception {
        mockMvc.perform(get("/products/1")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateProduct() throws Exception {
        mockMvc.perform(put("/products/1")
                .contentType("application/json")
                .content("{}"))
                .andExpect(status().isOk());
    }

    @Test
    public void testDeleteProduct() throws Exception {
        mockMvc.perform(delete("/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testSearchProducts() throws Exception {
        mockMvc.perform(get("/products/search?name=test")
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}