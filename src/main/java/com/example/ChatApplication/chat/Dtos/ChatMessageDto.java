package com.example.ChatApplication.chat.Dtos;

public class ChatMessageDto {

    private String contents;
    private Long fromUserId;
    private Long toUserId;

    public String getContents() {
        return contents;
    }

    public ChatMessageDto(Long fromUserId,Long toUserId,String contents){
        this.fromUserId=fromUserId;
        this.toUserId=toUserId;
        this.contents=contents;

    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }
}
