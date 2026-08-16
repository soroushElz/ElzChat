package com.example.ChatApplication.websocket;

import com.example.ChatApplication.Security.JwtService;
import com.example.ChatApplication.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.security.Principal;

@Component
public class ConnectionAuthenticationInterceptor implements ChannelInterceptor {

    @Autowired
    UserDetailsService userDetailsService;
    @Autowired
    JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor=StompHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);

        assert accessor!=null;
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");
            assert authorizationHeader != null;
            String token = authorizationHeader.substring(7);

            String username = jwtService.extractUsername(token);


            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User userDetails =(User) userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    accessor.setUser((Principal) usernamePasswordAuthenticationToken.getPrincipal());
                }
            }
        }
        return message;
    }
}

