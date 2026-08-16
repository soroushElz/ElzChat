package com.example.ChatApplication.chat.Dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
public record MessageRequest (@NotNull Long DestinationChannelId,
                              @NotNull @NotEmpty(message = "Message cannot be empty") String message,
                                Long replyTo ) {
    public boolean isReplyMessage(){
        return replyTo!=null;
    }
}