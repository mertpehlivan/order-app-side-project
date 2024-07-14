package com.mertdev.ecommerce.kafka.payment;

import com.mertdev.ecommerce.notification.Notification;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationRepository extends MongoRepository<Notification,String> {
}
