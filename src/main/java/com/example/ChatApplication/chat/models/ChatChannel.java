package com.example.ChatApplication.chat.models;

import com.example.ChatApplication.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name="chatChannel")
public class ChatChannel {
    @NotNull
    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name="userIdOne")
    private User userOne;

    @OneToOne
    @JoinColumn(name="userIdTwo")
    private User userTwo;

    public ChatChannel(){
    }

    public ChatChannel(User userOne,User UserTwo){
        this.userOne=userOne;
        this.userTwo=UserTwo;
    }

    public Long getId() {
        return id;
    }

    public User getUserOne() {
        return userOne;
    }

    public User getUserTwo() {
        return userTwo;
    }

    public void setUserTwo(User userTwo) {
        this.userTwo = userTwo;
    }

    public void setUserOne(User userOne) {
        this.userOne = userOne;
    }
}
