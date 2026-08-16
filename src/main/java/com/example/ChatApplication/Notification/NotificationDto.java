package com.example.ChatApplication.Notification;

import com.example.ChatApplication.user.dtos.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Builder
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private NotificationType notificationType;
    @JsonIgnoreProperties("notificationType")
    private INotificationPayload payload;
    private UserDto recipient;
    private LocalDateTime timeSent;

    @Override
    public String toString() {
        return "Notification{" +
                "notificationType=" + notificationType +
                ", payload=" + payload +
                ", recipient=" + recipient +
                ", timeSent=" + timeSent +
                '}';
    }
}
