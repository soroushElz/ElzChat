package com.example.ChatApplication.user;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Token {


    @Id
    @GeneratedValue(strategy=GenerationType.SEQUENCE)
    private Integer id;
    @Column(unique = true)
    private String token;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private LocalDateTime validatedAt;
    @Enumerated(EnumType.STRING)
    private TOKEN_TYPE tokenType;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name="userId",nullable = false)
    private User user;



}
