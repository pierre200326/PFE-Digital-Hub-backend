package com.example.backend.shop;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByPhoneModelIgnoreCase(String phoneModel);

    List<Product> findByPhoneModelIgnoreCaseAndProtectionTypeIgnoreCase(String phoneModel, String protectionType);
}