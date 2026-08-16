package com.example.ChatApplication.Notification;

import com.example.ChatApplication.Notification.Block.BlockNotificationPayload;
import com.example.ChatApplication.Notification.Error.ErrorNotificationPayload;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY,property = "notificationType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BlockNotificationPayload.class, name = "BLOCKED"),
        @JsonSubTypes.Type(value = ErrorNotificationPayload.class, name = "ERROR")
})
public interface INotificationPayload {

}
