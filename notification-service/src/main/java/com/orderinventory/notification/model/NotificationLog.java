package com.orderinventory.notification.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "notifications")
public class NotificationLog {

    @Id
    private String id;

    private String recipient;
    private String message;
    private LocalDateTime sentAt;

    public NotificationLog() {}

    public NotificationLog(String recipient, String message) {
        this.recipient = recipient;
        this.message = message;
        this.sentAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public String getRecipient() { return recipient; }
    public String getMessage() { return message; }
    public LocalDateTime getSentAt() { return sentAt; }
}
