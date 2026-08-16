package com.example.ChatApplication.chat.Dtos;

import com.example.ChatApplication.chat.models.ReactionAction;
import com.example.ChatApplication.chat.models.ReactionType;
import jakarta.validation.constraints.NotNull;

public record ReactionRequest(@NotNull Long messageId, @NotNull ReactionType reactionType, ReactionAction action) {

}
