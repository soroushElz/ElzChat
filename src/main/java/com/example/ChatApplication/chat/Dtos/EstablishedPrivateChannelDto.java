package com.example.ChatApplication.chat.Dtos;

import lombok.Getter;
import lombok.Setter;

public class EstablishedPrivateChannelDto {
    public EstablishedPrivateChannelDto(Long channelId, String userOneName, String userTwoName) {

        this.channelId=channelId;
        this.userOneName=userOneName;
        this.userTwoName=userTwoName;
    }

    @Setter
    @Getter
    private Long channelId;
    private final String userOneName;
    private final String userTwoName;

    public String GetUserOneName(){
        return userOneName;
    }

    public String GetUserTwoName(){
        return userTwoName;
    }
}

