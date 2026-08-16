package com.example.ChatApplication.Security;

import com.example.ChatApplication.Exception.ChannelNotFoundException;
import com.example.ChatApplication.Group.Entity.Group;
import com.example.ChatApplication.Group.Repository.GroupRepository;
import com.example.ChatApplication.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component("groupPreAuthorize")
public class GroupSecurityEvaluator {

    @Autowired
    GroupRepository groupRepository;

    public boolean isAdmin(Authentication authentication, String groupId){
        if(authentication==null || !authentication.isAuthenticated())
            return false;

        Group group=groupRepository.findById(Long.valueOf(groupId))
                .orElseThrow(()->new ChannelNotFoundException("group with id:"+groupId +" Not found"));

        return ((User) authentication.getPrincipal()).getId().equals(group.getAdmin().getId());


    }

    public boolean isMember(Authentication authentication, String groupId) {
        if(!authentication.isAuthenticated())
            return false;
        Group group=groupRepository.findById(Long.valueOf(groupId))
                .orElseThrow(()->new ChannelNotFoundException("group with id:"+groupId +" Not found"));

        return group.getGroupMembers().stream()
                .anyMatch(m->m.getId().equals(((User)authentication.getPrincipal()).getId()));
    }
}
