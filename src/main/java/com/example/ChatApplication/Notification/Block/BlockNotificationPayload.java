package com.example.ChatApplication.Notification.Block;

import com.example.ChatApplication.Notification.Block.BlockAction;
import com.example.ChatApplication.Notification.INotificationPayload;
import com.example.ChatApplication.user.dtos.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BlockNotificationPayload implements INotificationPayload {
    UserDto blockedBy;
    UserDto blockedUser;
    Instant timeBlocked;
    BlockAction action;
}
