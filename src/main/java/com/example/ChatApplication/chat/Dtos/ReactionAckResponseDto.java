package com.example.ChatApplication.chat.Dtos;

import com.example.ChatApplication.chat.models.ReactionType;
import com.example.ChatApplication.user.dtos.UserDto;
import lombok.*;

@Builder
@Data
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ReactionAckResponseDto  {
   String messageId;
   UserDto  user;
   ReactionType reactionType ;
   String Action;
}
