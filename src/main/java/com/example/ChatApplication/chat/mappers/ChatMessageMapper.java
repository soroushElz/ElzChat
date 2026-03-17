package com.example.ChatApplication.chat.mappers;

import com.example.ChatApplication.chat.Dtos.ChatMessageDto;
import com.example.ChatApplication.chat.models.ChatMessage;
import com.example.ChatApplication.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ChatMessageMapper {

    @Autowired
    public ChatMessageMapper(UserService userService){
        this.userService=userService;
    }

    private static UserService userService;

    public static List<ChatMessageDto> mapMessagesToChatDtos(List<ChatMessage> messagesByLatest) {
        return messagesByLatest.stream().
                map(m-> new ChatMessageDto(m.getWriter().getId(),m.getReciever().getId(),m.getContents()))
                 .collect(Collectors.toList());
    }

    public static ChatMessage mapMessageDtoToMessage(ChatMessageDto chatMessageDto) {
        return new ChatMessage(userService.getUser(chatMessageDto.getFromUserId())
                                  ,userService.getUser(chatMessageDto.getToUserId())
                                      ,chatMessageDto.getContents()
                                  );
    }





}
