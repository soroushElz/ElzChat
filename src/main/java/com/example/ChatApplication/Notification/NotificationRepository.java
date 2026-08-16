package com.example.ChatApplication.Notification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification,Long> {

    List<Notification> findByStatusAndRecipientIdAndCreatedDateAfter(Status status, Long recipient_id, LocalDateTime dateTime);

}
