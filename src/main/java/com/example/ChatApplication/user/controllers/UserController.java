package com.example.ChatApplication.user.controllers;

import com.example.ChatApplication.Notification.Notification;
import com.example.ChatApplication.Notification.NotificationService;
import com.example.ChatApplication.chat.Dtos.ChatChannelDto;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.services.UserService;
import com.example.ChatApplication.user.dtos.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class UserController {

    @Autowired
    UserService userService;
    @Autowired
    NotificationService notificationService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/contacts")
    public ResponseEntity<List<ChatChannelDto>> retrieveUserContact()
      {
    User requestingUser=userService.getUser(SecurityContextHolder.getContext());
     List<ChatChannelDto> chatsList=userService.retrieveChatsList(requestingUser);
        return new ResponseEntity<>(chatsList, HttpStatus.OK);
    }
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/requesting/info")
    public ResponseEntity<User> retrieveUserInfo()
     throws UsernameNotFoundException {
    User requestingUser=userService.getUser(SecurityContextHolder.getContext());
        return new ResponseEntity<>(requestingUser, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/notification/pending")
    public ResponseEntity<List<Notification>> receivePendingNotification (){
        User requestingUser=userService.getUser(SecurityContextHolder.getContext());
        List<Notification> notifications=notificationService.receivePendingNotificationByRecipient(requestingUser);
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }


    @PreAuthorize("isAuthenticated()")
    @PostMapping("/user/updateBlockList")
    public ResponseEntity<Void> blockOrUnBlockUser(@RequestBody UpdateBlockListRequest updateBlockListRequest,
                                                   Authentication auth){
        var user=(User) auth.getPrincipal();
        userService.updateUserBlockList(updateBlockListRequest,user);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
