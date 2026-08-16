package com.example.ChatApplication.user.services;

import com.example.ChatApplication.chat.Dtos.ChatChannelDto;
import com.example.ChatApplication.chat.mappers.ChatChannelMapper;
import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.Notification.Block.BlockAction;
import com.example.ChatApplication.Notification.NotificationService;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.UserRepository;
import com.example.ChatApplication.user.dtos.UpdateBlockListRequest;
import com.example.ChatApplication.user.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    ChatChannelMapper chatChannelMapper;
    @Autowired
    NotificationService notificationService;
    @Autowired
    SimpUserRegistry userRegistry;

    public User getUser(SecurityContext context)  throws UsernameNotFoundException{
        return getUser(context, ctx->(User)ctx.getAuthentication().getPrincipal());
    }

    public User getUser(String email)  throws UsernameNotFoundException{
        return getUser(email, e-> userRepository.findByEmail(e).get());
    }

    public User getUser(Long userId)
    throws UsernameNotFoundException{
      return getUser(userId,id-> userRepository.findById(userId).get());
    }
    public boolean isUserSubscribed(String username, String targetDestination) {
        SimpUser user= userRegistry.getUser(username);
        if(user != null){
            return  user.getSessions().stream()
                    .flatMap(simpSession -> simpSession.getSubscriptions().stream())
                    .anyMatch(sub->sub.getDestination().equals(targetDestination));
        }
        return false;
    }

    private<T> User getUser(T identifier,IUserRetrievalStrategy<T> strategy){
     User user=strategy.getUser(identifier);
     if(user==null) {throw new UsernameNotFoundException("user Not Found!");}
     return user;
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public List<ChatChannelDto> retrieveChatsList(User requestingUser) {
        Set<ChatChannel> chatChannelList= userRepository.findUserWithChats(requestingUser.getId()).get().getChats();
        return chatChannelMapper.mapChatChannelToChatChannelDto((chatChannelList));
    }


    private boolean isPresent(User recipientUser) {
        return recipientUser.isOnline();
    }

    public void setIsPresent(User user, boolean isPresent) {
        user.setOnline(isPresent);
        if(!isPresent) user.setLastOffline(LocalDateTime.now());
        userRepository.save(user);
    }

    public List<UserDto> getBlockListByUser(UserDto userDto) {
        User user=userRepository.findUserWithBlockedListById(userDto.getId()).orElseThrow(()->new UsernameNotFoundException("USER id DOES NOT EXISTS"));
        return user.getBlockedUsers().stream()
                .map(blockeduser -> new UserDto(blockeduser.getId(),blockeduser.getUsername())).toList();
    }

    @Transactional
    public void updateUserBlockList(UpdateBlockListRequest updateBlockListRequest, User principal) {

       var user=userRepository.findUserWithBlockedListById(principal.getId()).get();

               for(var blockUserId: updateBlockListRequest.blockUsersList()){
                  User userBlocked=userRepository.findById(blockUserId).orElseThrow(()->new UsernameNotFoundException("USER id DOES NOT EXISTS"));
                  if (user.getBlockedUsers().contains(userBlocked)) continue;
               user.getBlockedUsers().add(userBlocked);
               notificationService.sendBlockNotification(user,userBlocked, BlockAction.BLOCK);
               }

        for(var unBlockUserId: updateBlockListRequest.unBlockUsersList()){
            User userBlocked=userRepository.findById(unBlockUserId).orElseThrow(()->new UsernameNotFoundException("USER id DOES NOT EXISTS"));
            if (!user.getBlockedUsers().contains(userBlocked)) continue;
            user.getBlockedUsers().remove(userBlocked);
            notificationService.sendBlockNotification(user,userBlocked, BlockAction.UNBLOCK);
        }

    }


}
