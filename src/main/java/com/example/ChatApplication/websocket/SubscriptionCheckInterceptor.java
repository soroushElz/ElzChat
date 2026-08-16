package com.example.ChatApplication.websocket;

import com.example.ChatApplication.chat.ChatType;
import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.chat.repositories.ChatChannelRepository;
import com.example.ChatApplication.chat.services.ChatService;
import com.example.ChatApplication.Notification.NotificationService;
import com.example.ChatApplication.Group.Service.GroupService;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.dtos.UserDto;
import com.example.ChatApplication.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
@Primary
public class SubscriptionCheckInterceptor implements ChannelInterceptor {

    @Autowired
    ChatChannelRepository chatChannelRepository;
    @Autowired
    UserService userService;
    @Autowired
    ChatService chatService;
    @Autowired
    GroupService groupService;
    @Autowired
    NotificationService notificationService;


    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(message);
        if (  StompCommand.SEND.equals(headerAccessor.getCommand()))
        {
            List<String> url= Arrays.stream(headerAccessor.getDestination().split("/")).toList();
            String chatChannelId=url.get(url.indexOf("chat")+1);
            if(!checkIsAllowed(chatChannelId,headerAccessor.getUser()))
                return null;
        }
        return message;
    }

    public boolean checkIsAllowed(String chatChannelId, Principal principal) {
        Optional<ChatChannel> chatChannelOptional=chatChannelRepository.findById(Long.valueOf(chatChannelId));
        if (chatChannelOptional.isEmpty())  return false;///channel  does not exists
        var chatChannel=chatChannelOptional.get();
        var user=(User) principal;
        if(isMember(Long.parseLong(chatChannelId),user)){
            ChatType type=chatChannel.getChatType();
            return switch (type) {
                case PRIVATE_CHAT ->{
                     if(isUserBlockedByOtherUser(chatChannel.getId(), user)){
                         notificationService.sendAlertToUser("you are blocked!",user);
                         yield false;
                    }
                     yield true;
                }
                case GROUP_CHAT -> {
                    if(isUserBannedFromGroupChat(chatChannel, user)) {
                        notificationService.sendAlertToUser("you are banned from group!", user);
                        yield false;
                    }
                    yield true;
                }
            };
        }
        ////send not member alert
        notificationService.sendAlertToUser("you are not member of this chat channel",user);
      return false;
    }


    public boolean isUserBannedFromGroupChat(ChatChannel chatChannel, User user) {
       return groupService.GetGroupByChannelId(chatChannel.getId())
                 .getBannedUsersList()
                   .stream()
                     .anyMatch(bannedUser->bannedUser.getId().equals(user.getId()));
    }

    private boolean isUserBlockedByOtherUser(Long chatChannelId,User firstuser) {
        List<UserDto> chatMembers=chatChannelRepository.getMembersByChatId(chatChannelId);
        UserDto otherUser=chatMembers.stream().
                filter(user-> !((user.getId()).equals(firstuser.getId()))).findFirst().get();

         List<UserDto> blockedList= userService.getBlockListByUser(otherUser);
       return blockedList.stream().anyMatch(u->  u.getId().equals(firstuser.getId()));
    }

    private boolean isMember(long channelId, User user) {
        return chatService.isUserMember(channelId,user);
    }


}

