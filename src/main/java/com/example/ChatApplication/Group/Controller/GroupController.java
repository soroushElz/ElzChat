package com.example.ChatApplication.Group.Controller;

import com.example.ChatApplication.Group.DTO.CreateGroupRequest;
import com.example.ChatApplication.Group.DTO.GroupSummaryResponse;
import com.example.ChatApplication.Group.DTO.UpdateBanListRequest;
import com.example.ChatApplication.Group.DTO.addOrRemoveMemberRequest;
import com.example.ChatApplication.Group.Entity.Group;
import com.example.ChatApplication.Group.Service.GroupService;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.dtos.UserDto;
import com.example.ChatApplication.user.services.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/group")
@RequiredArgsConstructor
public class GroupController {
    @Autowired
    GroupService groupService;
    @Autowired
    UserService userService;

    @PreAuthorize("isAuthenticated()")
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/create")
    public ResponseEntity<GroupSummaryResponse> createNewGroup(@NotNull @RequestBody @Valid CreateGroupRequest createGroupRequest ){
         User admin=userService.getUser(SecurityContextHolder.getContext());
        Group groupCreated= groupService.createNewGroup(createGroupRequest.name(),createGroupRequest.memberIds(),admin);
        var groupCreatedSummary=mapGroupToGroupSummaryDto(groupCreated);
        return new ResponseEntity<>(groupCreatedSummary, HttpStatus.CREATED);
    }

    @PreAuthorize("@groupPreAuthorize.isAdmin(authentication,#groupId)")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{groupId}")
    public ResponseEntity<String> deleteGroup(@NotBlank @PathVariable(value = "groupId",required = true) String groupId){
        groupService.deleteGroupById(groupId);
        return new ResponseEntity<>( HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("@groupPreAuthorize.isAdmin(authentication,#groupId)")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/members/add")
    public ResponseEntity<Void> addMember(@NotBlank @RequestParam("groupId") String groupId,
                                           @RequestBody @NotNull addOrRemoveMemberRequest addMembers
    ){
        groupService.addMembersToGroup(addMembers.addNewMembers(),groupId);

        return new ResponseEntity<>(HttpStatus.OK);
    }



    @PreAuthorize("@groupPreAuthorize.isAdmin(authentication,#groupId)")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/members/remove")
    public ResponseEntity<Void> removeMember(@NotBlank @RequestParam("groupId") String groupId,
                                             @RequestBody @NotNull addOrRemoveMemberRequest removeMembers
    ){
        groupService.removeMembersFromGroup(removeMembers.removeMembers(),groupId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("@groupPreAuthorize.isAdmin(authentication,#groupId)")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/banList/update")
    public ResponseEntity<Void> updateBanList(@NotBlank @RequestParam("groupId") String groupId,
                                              @RequestBody @NotNull UpdateBanListRequest updateBanListRequest
    ){
        groupService.UpdateBanList(updateBanListRequest,groupId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PreAuthorize("@groupPreAuthorize.isMember(authentication,#groupId)")
    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{groupId}")
    public ResponseEntity<GroupSummaryResponse> getGroupInfo(@NotBlank @PathVariable(value = "groupId",required = true) String groupId,
                                               Authentication authentication
    ){

        Group group=groupService.GetGroupByChannelId(Long.valueOf(groupId));
        var groupSummary=mapGroupToGroupSummaryDto(group);
        return new ResponseEntity<>(groupSummary,HttpStatus.OK);

    }
    private GroupSummaryResponse mapGroupToGroupSummaryDto(Group group){
        List<UserDto> bannedUsers=(group.getBannedUsersList()!=null)? group.getBannedUsersList().stream().map(u->new UserDto(u.getId(), u.getName())).toList()
                : Collections.emptyList();
        return  GroupSummaryResponse.builder().
                chatChannelId(group.getChatChannel().getId()).
                groupId(group.getId()).
                name(group.getName()).
                admin(new UserDto(group.getAdmin().getId(), group.getAdmin().getName())).
                members(group.getGroupMembers().stream().map(u->new UserDto(u.getId(), u.getName())).toList()).
                bannedUsersList(bannedUsers).
                build();
    }

    @PreAuthorize("@groupPreAuthorize.isMember(authentication,#groupId)")
    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{groupId}/leave")
    public ResponseEntity<String> leaveGroup(@PathVariable(value = "groupId",required = true) String groupId,
                                            Authentication authentication
    ){
        User user=(User) authentication.getPrincipal();
        groupService.removeMembersFromGroup(List.of(user.getId()),groupId);
        return new ResponseEntity<>("you left the group with groupId:"+groupId,HttpStatus.OK);

    }

    ////join with invite link



}
