package com.example.ChatApplication.user.controllers;

import com.example.ChatApplication.chat.Dtos.ChatChannelDto;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.services.UserService;
import com.example.ChatApplication.user.dtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class UserController {

    UserService userService;


    @GetMapping("/user/requesting/chatslist")
    public ResponseEntity<List<ChatChannelDto>> retrieveRequestingUserFriendList(Principal principal)
      throws UsernameNotFoundException{
    User requestingUser=(User)principal;
     List<ChatChannelDto> chatsList=userService.retrieveChatsList(requestingUser);
        return new ResponseEntity<>(chatsList, HttpStatus.OK);
    }

    @GetMapping("/users/requesting/info")
    public ResponseEntity<UserDto> retrieveUserInfo()
     throws UsernameNotFoundException {
    User requestingUser=userService.getUser(SecurityContextHolder.getContext());
     UserDto userDto=new UserDto(requestingUser.getId(),
                           requestingUser.getUsername());
        return new ResponseEntity<>(userDto, HttpStatus.OK);
    }
}
