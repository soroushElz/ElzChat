package com.example.ChatApplication.user.services;

import com.example.ChatApplication.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class UserPresenceService implements ChannelInterceptor {

    @Autowired
    UserService userService;

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor stompDetails=StompHeaderAccessor.wrap(message);
        if(stompDetails.getCommand()==null)
            return;

        switch(stompDetails.getCommand()) {
            case CONNECT:
            case CONNECTED:
                toggleUserPresence(Objects.requireNonNull(stompDetails.getUser()).getName(), true);
                break;
            case DISCONNECT:
                toggleUserPresence(Objects.requireNonNull(stompDetails.getUser()).getName(), false);
                break;
            default:
                break;
        }
    }

    public void toggleUserPresence(String email, boolean isPresent){
      User user=userService.getUser(email);
      userService.setIsPresent(user,isPresent);
    }

}
