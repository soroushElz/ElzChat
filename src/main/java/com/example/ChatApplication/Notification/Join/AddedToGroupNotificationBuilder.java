package com.example.ChatApplication.Notification.Join;

import com.example.ChatApplication.Notification.INotificationBuilder;
import com.example.ChatApplication.Notification.NotificationDto;
import com.example.ChatApplication.Notification.NotificationType;
import com.example.ChatApplication.user.dtos.UserDto;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public  class  AddedToGroupNotificationBuilder implements INotificationBuilder<AddedToGroupPayload> {


    @Override
    public Class<AddedToGroupPayload> getPayloadType() {
      return AddedToGroupPayload.class;
    }

    @Override
    public NotificationDto create(AddedToGroupPayload  payload, UserDto recipient) {
        return NotificationDto.builder().notificationType(NotificationType.GROUP_EVENT)
                        .recipient(recipient)
                          .payload(payload).
                    timeSent(LocalDateTime.now()).
                build();
    }




}
