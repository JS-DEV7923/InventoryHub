package com.inventoryhub.notifications;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface NotificationAttemptRepository extends JpaRepository<NotificationAttempt, UUID> {
}
