package com.orderinventory.notification.service;

import com.orderinventory.notification.model.NotificationLog;
import com.orderinventory.notification.repository.NotificationLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationLogRepository repository;

    public NotificationService(NotificationLogRepository repository) {
        this.repository = repository;
    }

    public void sendOrderConfirmation(String username, Long orderId) {
        String message = "Hi " + username + ", your order #" + orderId + " has been placed successfully.";
        // In a real system this would call an email/SMS provider. Here we just log + persist.
        log.info("Sending notification to {}: {}", username, message);
        repository.save(new NotificationLog(username, message));
    }

    public List<NotificationLog> getAll() {
        return repository.findAll();
    }
}
