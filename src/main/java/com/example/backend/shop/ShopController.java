package com.example.backend.shop;

import com.example.backend.notification.NotificationService;
import com.example.backend.shop.dto.CreateOrderRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/shop")
public class ShopController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final NotificationService notificationService;

    public ShopController(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            NotificationService notificationService) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
        this.notificationService = notificationService;
    }

    @GetMapping("/products")
    public List<Product> getAllProducts(
            @RequestParam(required = false) String phoneModel,
            @RequestParam(required = false) String protectionType) {

        if (phoneModel != null && protectionType != null) {
            return productRepository.findByPhoneModelIgnoreCaseAndProtectionTypeIgnoreCase(phoneModel, protectionType);
        }

        if (phoneModel != null) {
            return productRepository.findByPhoneModelIgnoreCase(phoneModel);
        }

        return productRepository.findAll();
    }

    @Transactional
    @PostMapping("/orders")
    public Order createOrder(@Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Vous devez être connecté");
        }

        if (request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Commande vide");
        }

        Order order = new Order();
        order.setCustomerPhone(authentication.getName());
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus("PENDING");

        List<OrderItem> orderItems = new ArrayList<>();
        double total = 0;

        for (CreateOrderRequest.CreateOrderItemRequest itemRequest : request.items()) {
            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produit introuvable"));

            if (product.getStock() < itemRequest.quantity()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Stock insuffisant pour " + product.getName());
            }

            product.setStock(product.getStock() - itemRequest.quantity());
            productRepository.save(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(product.getPrice());

            orderItems.add(orderItem);
            total += product.getPrice() * itemRequest.quantity();
        }

        order.setItems(orderItems);
        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        StringBuilder orderedProducts = new StringBuilder();
        for (OrderItem item : orderItems) {
            if (orderedProducts.length() > 0) {
                orderedProducts.append(", ");
            }

            orderedProducts.append(item.getProduct().getName())
                    .append(" x")
                    .append(item.getQuantity());
        }

        notificationService.createAdminNotification(
                "Nouvelle commande passée par " + authentication.getName()
                        + " - commande #" + savedOrder.getId()
                        + " - Produits : " + orderedProducts);

        return savedOrder;
    }
}