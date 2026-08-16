package com.example.ChatApplication.Notification.Error;

import com.example.ChatApplication.Notification.INotificationPayload;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ErrorNotificationPayload implements INotificationPayload {
    String message;
    Instant timeBlocked;


}
