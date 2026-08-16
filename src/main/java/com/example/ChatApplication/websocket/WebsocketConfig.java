package com.example.ChatApplication.websocket;

import com.example.ChatApplication.Security.JwtService;
import com.example.ChatApplication.user.services.UserPresenceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.messaging.access.intercept.MessageMatcherDelegatingAuthorizationManager;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.List;


@Configuration
@EnableWebSocketMessageBroker
@Order(Ordered.HIGHEST_PRECEDENCE+99)
@Slf4j
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws");
        registry.addEndpoint("/ws").withSockJS();
    }

    ;


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
    }


    @Override
    public void configureClientInboundChannel(ChannelRegistration registration){
       registration.interceptors(ConnectionInterceptor(),subscriptionInterceptor(),presenceChannelInterceptor());
        registration.taskExecutor().corePoolSize(8);
    }

    @Bean
    protected ChannelInterceptor subscriptionInterceptor() {
        return new SubscriptionCheckInterceptor();
    }


    @Override
    public void configureClientOutboundChannel(ChannelRegistration registration) {
        registration.interceptors(presenceChannelInterceptor());
        registration.taskExecutor().corePoolSize(8);
    }

    @Bean
    protected ChannelInterceptor presenceChannelInterceptor() {
        return new UserPresenceService();
    }

    @Bean
    protected ConnectionAuthenticationInterceptor ConnectionInterceptor(){
        return new ConnectionAuthenticationInterceptor();
}


}




