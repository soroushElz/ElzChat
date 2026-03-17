package com.example.ChatApplication.user.services;

import com.example.ChatApplication.chat.Dtos.ChatChannelDto;
import com.example.ChatApplication.chat.mappers.ChatChannelMapper;
import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.chat.repositories.ChatChannelRepository;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.UserRepository;
import com.example.ChatApplication.user.dtos.NotificationDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private UserRepository userRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ChatChannelRepository chatchannelRespository;

    public User getUser(SecurityContext context)  throws UsernameNotFoundException{
        return getUser(context, ctx->(User)ctx.getAuthentication().getPrincipal());
    }

    public User getUser(String email)  throws UsernameNotFoundException{
        return getUser(email, e->
                         userRepository.findByEmail((String)e).get());
    }

    public User getUser(Long userId)
    throws UsernameNotFoundException{
      return getUser(userId,id->
                             userRepository.findById(userId).get());
    }

    private<T> User getUser(T identifier,IUserRetrievalStrategy<T> strategy){
     User user=strategy.getUser(identifier);
     if(user==null) {throw new UsernameNotFoundException("user Not Found!");}
     return user;
    }



    public List<ChatChannelDto> retrieveChatsList(User requestingUser) {
        List<ChatChannel> chatChannelList=chatchannelRespository.getChatsListByUserId(requestingUser.getId());
        return ChatChannelMapper.mapChatChannelToChatChannelDto(chatChannelList);
    }
    public boolean doesUserExist(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        return user.isEmpty();
    }


    public void notifyUser(User recipientUser, NotificationDto notificationDto) {
        if(this.isPresent(recipientUser)) {
            simpMessagingTemplate
                    .convertAndSend("/topic/user.notification." + recipientUser.getId(), notificationDto);
        }}
    private boolean isPresent(User recipientUser) {
        return recipientUser.isOnline();
    }

    public void setIsPresent(User user, boolean isPresent) {
        user.setOnline(isPresent);
        userRepository.save(user);
    }
}
