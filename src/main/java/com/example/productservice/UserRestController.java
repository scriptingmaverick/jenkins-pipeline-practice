package com.example.productservice;

import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserRestController {
    @GetMapping
    public List<User> getAllUsers() {
        return Arrays.asList(new User("John Doe", "john@example.com"));
    }

    static class User {
        private String name;
        private String email;
        public User(String name, String email) {
            this.name = name;
            this.email = email;
        }
        // Getters omitted for brevity
    }
}