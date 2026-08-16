package com.example.ChatApplication.chat.controllers;
import com.example.ChatApplication.Commons.PageResponse;
import com.example.ChatApplication.Exception.ChannelNotFoundException;
import com.example.ChatApplication.chat.Dtos.*;
import com.example.ChatApplication.chat.repositories.SearchFilters;
import com.example.ChatApplication.chat.services.ChatService;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.dtos.UserDto;
import com.example.ChatApplication.user.services.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@Validated
public class ChatController {

    @Autowired
    ChatService chatService;

    @Autowired
    UserService userService;


      @PostMapping("/chat/newChannel")
      public ResponseEntity<EstablishedPrivateChannelDto> establishPrivateChatChannel(@RequestBody PrivateChatInitializationDto
                                                                                           chatChannelInitializationDto
                                                                                           , Authentication auth)
          {

            Long channelId = chatService.establishPrivateChatSession(chatChannelInitializationDto);
            User userOne = (User) auth.getPrincipal();
            User userTwo = userService.getUser(chatChannelInitializationDto.getUserTwoId());
          EstablishedPrivateChannelDto establishedChannelDto = new EstablishedPrivateChannelDto(channelId, userOne.getUsername(), userTwo.getUsername());
            return ResponseEntity.ok(establishedChannelDto);
}           
      @MessageMapping ("/chat/{channelId}/forward")
      public void forwardMessage(@Payload @Valid  ForwardMessageRequest forwardRequest,StompHeaderAccessor headerAccessor) {
          User user = (User) headerAccessor.getUser();
          chatService.forwardMessage(forwardRequest, user);
      }
 @PreAuthorize("isAuthenticated() && @ChatPreAuthz.getIsUserInChat(authentication,#channelId) ")
@PostMapping("/chat/{channelId}/search")
     public ResponseEntity<List<ChatMessageDto>> searchChat(@PathVariable String channelId, @RequestBody @Valid List<SearchFilters> filters
                                                                         ,@RequestParam( name="pageSize",defaultValue= "10" ) Integer pageSize,
                                                                          @RequestParam(name="pageNum",defaultValue = "0")Integer pageNum ) {

      List<ChatMessageDto> messages=chatService.searchChat(channelId, filters,PageRequest.of(pageNum,pageSize));
        return ResponseEntity.ok(messages);
     }


    @PreAuthorize("@ChatPreAuthz.getCanUserMakeReaction(authentication,#messageId)")
    @PostMapping("/message/{messageId}/reaction")
    public  ResponseEntity<ReactionAckResponseDto> sendReaction(@PathVariable("messageId")String messageId,@Valid @RequestBody ReactionRequest reactionRequest)
    {
        var user =userService.getUser(SecurityContextHolder.getContext());
        ReactionAckResponseDto  responseDto= chatService.makeReaction(reactionRequest,user);
        return new ResponseEntity<>(responseDto, HttpStatus.OK);
    }


    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chat/messages/{channelId}")
    public ResponseEntity<PageResponse<ChatMessageDto>> getChatMessages(@PathVariable("channelId") String channelId,
                                                                @RequestParam( name="pageSize",defaultValue= "20" ) Integer pageSize,
                                                                 @RequestParam(name="pageNum",defaultValue = "0")Integer pageNum
                                                                      ) throws ChannelNotFoundException {
       Pageable pageRequest= PageRequest.of(pageNum,pageSize);
       List<ChatMessageDto> chatMessageDTOs= (chatService.getAllChatMessagesByChannelId(Long.valueOf(channelId), pageRequest));
        return ResponseEntity.ok().body(PageResponse.<ChatMessageDto> builder()
                                    .content(chatMessageDTOs)
                                     .size(pageSize)
                                      .number(pageNum)
                                       .build()
        );
    }
    ///get all new UnDelivered  messages
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/chat/messages/pending")
    public ResponseEntity<List<ChatMessageDto>> getAllUnDeliveredChatMessages(@RequestParam(required = false,name = "channelId") Optional<Long> channelId
                                                       , Authentication authentication) throws ChannelNotFoundException {

        List<ChatMessageDto> messageDtos= (channelId.isPresent()) ?
                 chatService.getUserUnseenChatMessagesByChannelId(channelId.get())
                : chatService.getAllUndeliveredChatMessagesByRecipient((User)authentication.getPrincipal());


        return ResponseEntity.ok().body(messageDtos);
    }

    @MessageMapping("/chat/{channelId}")
     public void sendMessage(@Payload @Valid MessageRequest message, StompHeaderAccessor headerAccessor)
       throws BeansException {

        User user =(User) headerAccessor.getUser();
        var chatMessageDto =  ChatMessageDto.builder().content(message.message())
                  .senderDto(new UserDto(user.getId(), user.getUsername())).
                destinationChannelId(message.DestinationChannelId())
                  .timeSent(LocalDateTime.now())
                .ReplyTo(message.isReplyMessage()? message.replyTo() : null)
                  .build();


        if (message.isReplyMessage()) {
            chatService.SubmitMessageReply(chatMessageDto);
        } else {
            chatService.SubmitChatMessage(chatMessageDto);
        }

    }


}
