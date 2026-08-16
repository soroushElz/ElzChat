package com.example.ChatApplication.chat.Dtos;

import com.example.ChatApplication.chat.models.ReactionAggregate;
import com.example.ChatApplication.user.dtos.UserDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.annotation.Nullable;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Getter
@Setter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class ChatMessageDto {

    @Override
    public String toString() {
        return "ChatMessageDto{" +
                "content='" + content + '\'' +
                ", timeSent=" + timeSent +
                ", senderDto=" + senderDto +
                ", destinationChannelId=" + destinationChannelId +
                ", forwarded=" + forwarded +
                ", messageId=" + messageId +
                 (ReplyTo!=null?", ReplyTo="+ ReplyTo :"") +
                (originalSender!=null?", originalSender="+ originalSender :"") +
                (reactions!=null?", reactions="+ reactions :"") +
                '}';
    }

    public ChatMessageDto() {}

    private  String content;

    private LocalDateTime timeSent;

    private  UserDto senderDto;

    private  Long destinationChannelId;
   private  boolean forwarded;
   @Nullable
    private  Long messageId;
    @Nullable
    private  Long ReplyTo;
    @Nullable
    private  UserDto originalSender;
    @Nullable
    private  List<ReactionAggregate> reactions;




    ////Response




}
