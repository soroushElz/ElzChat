package com.example.ChatApplication.Notification;

import com.example.ChatApplication.user.dtos.UserDto;

public interface INotificationBuilder<T extends INotificationPayload> {
    Class<T> getPayloadType();



    NotificationDto create(T payload, UserDto recipients);
}
