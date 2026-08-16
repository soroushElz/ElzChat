package com.example.ChatApplication.Security;

import com.example.ChatApplication.chat.models.ChatMessage;
import com.example.ChatApplication.chat.repositories.ChatMessageRepository;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.websocket.SubscriptionCheckInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Optional;

@RequiredArgsConstructor
@Component(value = "ChatPreAuthz")
public class ChatControllerSecurityEvaluator {

    @Autowired
    ChatMessageRepository messageRepository;
    @Autowired
    SubscriptionCheckInterceptor securityEvaluator;


    public boolean getCanUserMakeReaction(Authentication authentication, String messageId) {
        Optional<ChatMessage> message = messageRepository.findById(Long.valueOf(messageId));
        return message
                .filter(chatMessage -> securityEvaluator.checkIsAllowed(String.valueOf(chatMessage.getDestinationId()), (Principal) authentication.getPrincipal()))
                   .isPresent();

    }

    public boolean getIsUserInChat(Authentication authentication,String channelId ) {
        return securityEvaluator.checkIsAllowed(channelId,(Principal) authentication.getPrincipal());
    }
}