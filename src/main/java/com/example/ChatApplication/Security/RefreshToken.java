package com.example.ChatApplication.Security;

import com.example.ChatApplication.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Entity(name="refreshToken")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE )
    private Long id;

    @OneToOne
    @JoinColumn(name="users_id")
    private User user;

    @Column(nullable = false,unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;
}
