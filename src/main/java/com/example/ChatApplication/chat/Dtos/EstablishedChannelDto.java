package com.example.ChatApplication.chat.Dtos;

import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

public class EstablishedChannelDto {
    public EstablishedChannelDto(Long channelId, String userOneName, String userTwoName) {

        this.channelId=channelId;
        this.userOneName=userOneName;
        this.userTwoName=userTwoName;
    }

    @Setter
    @Getter
    private Long channelId;
    private String userOneName;
    private String userTwoName;

    public String GetUserOneName(){
        return userOneName;
    }

    public String GetUserTwoName(){
        return userTwoName;
    }
}

