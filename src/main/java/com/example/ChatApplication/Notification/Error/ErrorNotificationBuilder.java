package com.example.ChatApplication.Notification.Error;

import com.example.ChatApplication.Notification.INotificationBuilder;
import com.example.ChatApplication.Notification.NotificationDto;
import com.example.ChatApplication.Notification.NotificationType;
import com.example.ChatApplication.user.dtos.UserDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ErrorNotificationBuilder implements INotificationBuilder<ErrorNotificationPayload> {
    @Override
    public Class<ErrorNotificationPayload> getPayloadType() {
        return ErrorNotificationPayload.class;
    }

    @Override
    public NotificationDto create(ErrorNotificationPayload payload, UserDto recipient) {
        return NotificationDto.builder().notificationType(NotificationType.ERROR)
                .recipient(recipient)
                .payload(payload).
                 timeSent(LocalDateTime.now()).
                build();
    }
}
