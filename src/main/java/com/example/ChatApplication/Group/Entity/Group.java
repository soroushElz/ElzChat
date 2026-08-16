package com.example.ChatApplication.Group.Entity;

import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Setter
@Getter
@Table(name = "`group`")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Group {
    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    private ChatChannel chatChannel;
    private String name;

    @ManyToOne(fetch=FetchType.LAZY)
    private User admin;

   @ManyToMany(fetch=FetchType.EAGER)
   @JoinTable(name = "banned_users",joinColumns = @JoinColumn(name = "group_id"),
           inverseJoinColumns = @JoinColumn(name = "user_id"))
   private Set<User> bannedUsersList;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "group_members",joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "member_id"))
   private Set<User> groupMembers;

}
