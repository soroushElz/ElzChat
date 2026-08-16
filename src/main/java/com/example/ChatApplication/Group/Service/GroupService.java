package com.example.ChatApplication.Group.Service;

import com.example.ChatApplication.Exception.ChannelNotFoundException;
import com.example.ChatApplication.chat.ChatType;
import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.Group.DTO.UpdateBanListRequest;
import com.example.ChatApplication.Group.Entity.Group;
import com.example.ChatApplication.Group.Repository.GroupRepository;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.UserRepository;
import com.example.ChatApplication.user.services.UserService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;

    private final UserRepository userRepository;

    private final UserService userService;

    public Group GetGroupByChannelId(Long chatId) {
        var group= groupRepository.findByChatChannelId(chatId);
        return group.orElseThrow(()-> new NoSuchElementException("group not found"));
    }

    @Transactional
    public Group createNewGroup(String name, List<Long> userIds, User Admin) {
        Set<User> users =userRepository.findByIdIsIn(userIds);
        users.add(userService.getUser(SecurityContextHolder.getContext()));////add creater User to members
        Group group=new Group();
          group.setGroupMembers(users);
          group.setName(name);
          group.setAdmin(Admin);
        ChatChannel groupChatChannel = new ChatChannel();
          groupChatChannel.setChatType(ChatType.GROUP_CHAT);
          users.forEach(groupChatChannel::addUser);
        group.setChatChannel(groupChatChannel);
       return  groupRepository.save(group);

    }

    @Transactional
    public void deleteGroupById(String groupId) {
        Group group =groupRepository.findById(Long.valueOf(groupId)).orElseThrow();
        groupRepository.delete(group);
    }

    @Transactional
    public void addMembersToGroup(List<Long> userIds, String groupId) {
        if (userIds==null || userIds.isEmpty())
            throw new UsernameNotFoundException("user ids aren't provided");
        Group group=groupRepository.findById(Long.valueOf(groupId)).orElseThrow(()->new ChannelNotFoundException("group with id:"+groupId +" Not found"));

        ChatChannel groupChatChannel=group.getChatChannel();

        group.getGroupMembers().addAll(userIds.stream()
                        .map(id->{User user=userRepository.findById(id).orElse(null);
                                    assert user != null;
                                    user.addChatChannel(groupChatChannel);
                                     return user;})
                 .collect(Collectors.toSet()));

    }

    @Transactional
    public void removeMembersFromGroup(List<Long> userIds, String groupId) {
        if (userIds==null || userIds.isEmpty())
            throw new UsernameNotFoundException("user ids aren't provided");
        Group group=groupRepository.findById(Long.valueOf(groupId)).orElseThrow(()->new ChannelNotFoundException("group with id:"+groupId +" Not found"));
        if(userIds.contains(group.getAdmin().getId())) {         ////if admin leaves the group ,group is deleted
            deleteGroupById(groupId);
            return;
        }

        ChatChannel groupChatChannel=group.getChatChannel();
        group.getGroupMembers().removeAll(userIds.stream()
                .map(id->{
                    User user=userRepository.findById(id).orElse(null);
                    assert user != null;
                    user.removeChat(groupChatChannel);
                    return user;
                })
                .collect(Collectors.toSet())
        );

    }
    @Transactional
    public void UpdateBanList(@NotNull UpdateBanListRequest updateBanListRequest, String groupId) {

        Group group=groupRepository.findById(Long.valueOf(groupId)).orElseThrow(()->new ChannelNotFoundException("group with id:"+groupId +" Not found"));

        group.getBannedUsersList().addAll(updateBanListRequest.addUsersToBanlist().stream()
                .map(id->userRepository.findById(id).orElse(null))
                .collect(Collectors.toSet()));

        group.getBannedUsersList().removeAll(updateBanListRequest.removeUsersFromBanlist().stream()
                .map(id->userRepository.findById(id).orElse(null))
                .collect(Collectors.toSet()));

    }
}
