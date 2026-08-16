package com.example.ChatApplication.chat.models;

import com.example.ChatApplication.chat.ChatType;
import com.example.ChatApplication.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.engine.internal.Cascade;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name="chat_channel")
public class ChatChannel {
    @NotNull
    @Id
    @GeneratedValue(strategy =GenerationType.SEQUENCE)
    private Long id;

    @Enumerated(EnumType.STRING)
    ChatType chatType;


    @ManyToMany(fetch =FetchType.EAGER)
    @BatchSize(size = 20)
    @JoinTable(
            name = "chat_members",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id"))
    private Set<User> chatMembers=new HashSet<>();



    public void addUser(User user){
        this.chatMembers.add(user);
        user.getChats().add(this);
    }

    public void removeUser(User user){
        this.chatMembers.remove(user);
        user.getChats().remove(this);
    }

    public ChatChannel(){
    }




}
