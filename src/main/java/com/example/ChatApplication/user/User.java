package com.example.ChatApplication.user;

import com.example.ChatApplication.Role.Role;
import com.example.ChatApplication.chat.models.ChatChannel;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Builder
@Table(name = "users")
public class User implements UserDetails,Principal {

    public User(Long id, String email){
        this.id=id;
        this.email=email;
    }

    public User(Long id) {
        this.id = id;
    }

    @Getter
    @ManyToMany(mappedBy = "chatMembers",fetch = FetchType.EAGER)
    @Builder.Default
    private Set<ChatChannel> chats=new HashSet<>();

    @BatchSize(size = 20)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "blocked_Users",joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_user_id"))
    private Set<User> blockedUsers=new HashSet<>();

    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Long id;
    private String firstname;
    private String lastname;
    @Column(unique = true)
    private String email;
    private String password;
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;
    @CreatedDate
    @Column(nullable = false,updatable = false)
    private LocalDateTime createdDate;
    @LastModifiedDate
    @Column(insertable = false )
    private LocalDateTime lastModifiedDate;
    private boolean accountLocked;
    private boolean isOnline;
    private LocalDateTime lastOffline;
    public void addChatChannel(ChatChannel chat){
        this.chats.add(chat);
        chat.getChatMembers().add(this);
    }

    public void removeChat(ChatChannel chatChannel){
        this.chats.remove(chatChannel);
        chatChannel.getChatMembers().remove(this);
    }


    public void addRole(Role role){  this.roles.add(role);  }

    @Override
    public String getName() {
        return email;
    }

    @Override
    public Collection<SimpleGrantedAuthority> getAuthorities() {
        return this.roles
                .stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_"+role.getName()))
                .collect(Collectors.toList());
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !accountLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean isOnline(){return isOnline;}


}
