package com.example.ChatApplication.Notification.Join;

import com.example.ChatApplication.Notification.INotificationPayload;
import com.example.ChatApplication.chat.Dtos.ChatChannelDto;
import com.example.ChatApplication.user.dtos.UserDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AddedToGroupPayload implements INotificationPayload {
    private ChatChannelDto chatChannelDto;
    private UserDto Admin;
    private LocalDateTime timeJoined;

}
