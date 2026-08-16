package com.example.ChatApplication.Group.DTO;

import com.example.ChatApplication.user.dtos.UserDto;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupSummaryResponse(Long groupId,
                                   Long chatChannelId,
                                   UserDto admin,
                                    String name,
                                    List<UserDto> bannedUsersList,
                                    List<UserDto> members) {
    @Override
    public String toString() {
        return "GroupSummaryResponse{" +
                "groupId=" + groupId +
                ", chatChannelId=" + chatChannelId +
                ", admin=" + admin +
                ", name='" + name + '\'' +
                ", bannedUsersList=" + bannedUsersList +
                ", members=" + members +
                '}';
    }
}
