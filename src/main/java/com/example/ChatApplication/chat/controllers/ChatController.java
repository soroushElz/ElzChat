package com.example.ChatApplication.chat.controllers;

import com.example.ChatApplication.Exception.IsSameUserException;
import com.example.ChatApplication.chat.Dtos.ChatMessageDto;
import com.example.ChatApplication.chat.Dtos.EstablishedChannelDto;
import com.example.ChatApplication.chat.services.ChatService;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import com.example.ChatApplication.chat.Dtos.ChatChannelInitializationDto;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class ChatController {

    @Autowired
    ChatService chatService;

    @Autowired
    UserService userService;


@PutMapping("/private-chat/channel")
        public ResponseEntity<EstablishedChannelDto> establishChatChannel(@RequestBody ChatChannelInitializationDto chatChannelInitializationDto
                                                             , Principal principal)
         throws IsSameUserException, UsernameNotFoundException {

    Long channelId = chatService.establishChatSession(chatChannelInitializationDto);
    User userOne = (User) principal;
    User userTwo = userService.getUser(chatChannelInitializationDto.getUserTwoId());
    EstablishedChannelDto establishedChannelDto = new EstablishedChannelDto(channelId, userOne.getUsername(), userTwo.getUsername());
    return ResponseEntity.ok(establishedChannelDto);
}
@GetMapping("/private-chat/channel/{channelId}")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(@PathVariable Long channelId,
                                                                @AuthenticationPrincipal UserDetails userDetails)
            throws ChannelNotFoundException {
        User user = userService.getUser(userDetails.getUsername());
        List<ChatMessageDto> chatMessageDtos = chatService.getChatMessages(channelId);
        return ResponseEntity.ok().body(chatMessageDtos);
    }

@MessageMapping("/private-chat.{channelId}")
@SendTo("/topic/private-chat.{channelId}")
 public ChatMessageDto sendMessage(@DestinationVariable String channelId, ChatMessageDto chatMessage)
       throws BeansException, UserNotFoundException {
        chatService.SubmitChatMessage(chatMessage);
        return chatMessage;
    }
}
