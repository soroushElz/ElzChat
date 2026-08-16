package com.example.ChatApplication.chat.repositories;

import com.example.ChatApplication.chat.models.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageReactionRepository extends JpaRepository <MessageReaction,Long> {

    @Query("SELECT r FROM MessageReaction r WHERE r.message.id = :messageId AND r.user.id = :userId")
    Optional<MessageReaction> findMessageReactionByMessageIdAndUserId(@Param("messageId") Long messageId,@Param("userId") Long UserId);
}
