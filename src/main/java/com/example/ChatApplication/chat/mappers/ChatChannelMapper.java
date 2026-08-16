package com.example.ChatApplication.chat.mappers;

import com.example.ChatApplication.chat.ChatType;
import com.example.ChatApplication.chat.Dtos.ChatChannelDto;
import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.Group.Service.GroupService;
import com.example.ChatApplication.user.dtos.UserDto;
import com.example.ChatApplication.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ChatChannelMapper {

    @Autowired
    GroupService groupService;
    @Autowired
    UserService userService;

    public  List<ChatChannelDto> mapChatChannelToChatChannelDto(Set<ChatChannel> chatChannelList) {

        return chatChannelList.stream()
                                .map(c->
                                        new ChatChannelDto(c.getChatMembers().stream().map(m->new UserDto(m.getId(),m.getUsername())).toList()
                                                          ,c.getChatType()
                                                           ,c.getId()
                                                          ,getChannelName(c))
                                )
                                        .collect(Collectors.toList());
    }

    private  String getChannelName(ChatChannel channel) {
        if (channel.getChatType()==ChatType.PRIVATE_CHAT){
            var userRequesting =userService.getUser(SecurityContextHolder.getContext());
            return channel.getChatMembers().stream().filter(member-> !Objects.equals(member.getId(), userRequesting.getId()))
                    .toList().get(0).getUsername();
           } else{
            var group=groupService.GetGroupByChannelId(channel.getId());
            return group.getName();
        }
    }
}
