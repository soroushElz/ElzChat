package com.example.ChatApplication.chat.Dtos;

public class ChatChannelInitializationDto {

    private Long userOneId;
    private Long userTwoId;

    public Long getUserOneId() {
        return userOneId;
    }

    public void setUserOneId(Long userOneId) {
        this.userOneId = userOneId;
    }

    public Long getUserTwoId() {
        return userTwoId;
    }

    public void setUserTwoId(Long userTwoId) {
        this.userTwoId = userTwoId;
    }
}
