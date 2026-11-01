package com.orderinventory.inventory.service;

import com.orderinventory.inventory.dto.ProductRequest;
import com.orderinventory.inventory.model.Product;
import com.orderinventory.inventory.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InventoryService {

    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);

    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAll() {
        return productRepository.findAll();
    }

    public Product create(ProductRequest request) {
        Product product = new Product(request.getName(), request.getDescription(),
                request.getPrice(), request.getStockQuantity());
        return productRepository.save(product);
    }

    public boolean isAvailable(String productId, int qty) {
        Optional<Product> product = productRepository.findById(productId);
        return product.isPresent() && product.get().getStockQuantity() >= qty;
    }

    /** Called asynchronously from the Kafka consumer when an OrderPlacedEvent arrives. */
    public void decrementStock(String productId, int qty) {
        productRepository.findById(productId).ifPresentOrElse(product -> {
            int remaining = Math.max(0, product.getStockQuantity() - qty);
            product.setStockQuantity(remaining);
            productRepository.save(product);
            log.info("Decremented stock for productId={} by {} -> remaining={}", productId, qty, remaining);
        }, () -> log.warn("Received stock decrement for unknown productId={}", productId));
    }
}
