package com.example.ChatApplication.Notification.Block;

import com.example.ChatApplication.Notification.Block.BlockNotificationPayload;
import com.example.ChatApplication.Notification.INotificationBuilder;
import com.example.ChatApplication.Notification.NotificationDto;
import com.example.ChatApplication.Notification.NotificationType;
import com.example.ChatApplication.user.dtos.UserDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class BlockNotificationBuilder implements INotificationBuilder<BlockNotificationPayload> {
    @Override
    public Class<BlockNotificationPayload> getPayloadType() {
        return BlockNotificationPayload.class;
    }

    @Override
    public NotificationDto create(BlockNotificationPayload payload, UserDto recipient) {
        return NotificationDto.builder().notificationType(NotificationType.BLOCKED)
                .recipient(recipient)
                .payload(payload).
                timeSent(LocalDateTime.now()).
                build();
    }
}
