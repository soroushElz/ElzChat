package com.example.ChatApplication.chat.Dtos;

import com.example.ChatApplication.chat.ChatType;
import com.example.ChatApplication.user.dtos.UserDto;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatChannelDto {


    private  List<UserDto> users;
    private  ChatType type;
    private Long channelId;
    private  String name;
}
