package com.example.ChatApplication.chat.Dtos;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ForwardMessageRequest(@NotNull @NotEmpty List<Long> messageIds, @NotNull Long SourceChatId, @NotNull Long destinationChatId) {

}
