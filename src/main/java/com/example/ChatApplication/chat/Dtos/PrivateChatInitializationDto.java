package com.example.ChatApplication.chat.Dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PrivateChatInitializationDto {

    private Long userOneId;
    private Long userTwoId;


    public PrivateChatInitializationDto(Long userOneId, Long userTwoId) {
        this.userOneId = userOneId;
        this.userTwoId = userTwoId;
    }

}
