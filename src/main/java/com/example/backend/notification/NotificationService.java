package com.example.backend.notification;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createAdminNotification(String message) {
        Notification notification = new Notification();
        notification.setMessage(message);
        notification.setTargetRole("ADMIN");
        notification.setRead(false);
        return notificationRepository.save(notification);
    }

    public List<Notification> getAdminNotifications() {
        return notificationRepository.findByTargetRoleOrderByCreatedAtDesc("ADMIN");
    }

    public long countUnreadAdminNotifications() {
        return notificationRepository.countByTargetRoleAndIsReadFalse("ADMIN");
    }

    public Notification markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification introuvable"));

        notification.setRead(true);
        return notificationRepository.save(notification);
    }

    public void markAllAsReadForAdmin() {
        List<Notification> notifications = notificationRepository.findByTargetRoleOrderByCreatedAtDesc("ADMIN");
        for (Notification notification : notifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
}