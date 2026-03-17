package com.example.ChatApplication.user.dtos;

public class UserDto {

    public UserDto(){}

    private Long id;

    private String username;

    public UserDto(Long id,String userName){
        this.username=userName;
        this.id=id;
    }

    public Long getId(){
        return id;
    }

    public String getUsername(){
        return username;
    }
}
