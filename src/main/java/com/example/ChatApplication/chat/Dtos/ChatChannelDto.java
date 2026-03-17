package com.example.ChatApplication.chat.Dtos;

import com.example.ChatApplication.user.dtos.UserDto;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ChatChannelDto {
    UserDto userOne;
    UserDto userTwo;
    Long ChannelId;

    public ChatChannelDto(){}


    public ChatChannelDto(UserDto userDtoOne, UserDto userDtoTwo, Long id) {
        this.userOne=userDtoOne;
        this.userTwo=userDtoTwo;
        this.ChannelId=id;
    }
}
