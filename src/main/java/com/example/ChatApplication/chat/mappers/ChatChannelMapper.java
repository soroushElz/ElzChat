package com.example.ChatApplication.chat.mappers;

import com.example.ChatApplication.chat.Dtos.ChatChannelDto;
import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.user.dtos.UserDto;

import java.util.List;
import java.util.stream.Collectors;

public class ChatChannelMapper {


    public static List<ChatChannelDto> mapChatChannelToChatChannelDto(List<ChatChannel> chatChannelList) {
        return chatChannelList.stream()
                                .map(c->new ChatChannelDto(new UserDto(c.getUserOne().getId(),c.getUserOne().getUsername())
                                                           ,new UserDto(c.getUserTwo().getId(),c.getUserTwo().getUsername())
                                                            ,c.getId()))
                                                  .collect(Collectors.toList());
    }
}
