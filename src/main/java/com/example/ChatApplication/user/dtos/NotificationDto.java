package com.example.ChatApplication.user.dtos;

public class NotificationDto {
    public NotificationDto(String type, String content, Long fromUserId) {
    this.content=content;
    this.type=type;
    this.fromUserId=fromUserId;
    }

    public NotificationDto() {
    }

    private String type;

    private String content;

    private Long fromUserId;


    public String getType(){
        return type;
    }

    public String getContent(){
        return content;
    }

    public Long getFromUserId(){
        return fromUserId;
    }





}
