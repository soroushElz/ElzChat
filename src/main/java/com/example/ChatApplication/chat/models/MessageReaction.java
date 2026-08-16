package com.example.ChatApplication.chat.models;

import com.example.ChatApplication.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Builder
@Entity
@Table(
        name = "message_reactions",
        uniqueConstraints ={
                @UniqueConstraint(
                        columnNames = {"message_id", "user_id"}
                )
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class MessageReaction {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long Id;

   @ManyToOne(fetch = FetchType.LAZY,optional = false)
   User user;
   @ManyToOne(fetch=FetchType.LAZY,optional = false)
   ChatMessage message;
   @Enumerated(EnumType.STRING)
    ReactionType type;
   Instant createdAt;


}

