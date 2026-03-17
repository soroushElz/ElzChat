package com.example.ChatApplication.chat.repositories;

import com.example.ChatApplication.chat.models.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> {



@Query("FROM " +
        " ChatMessage " +
        "m" +
        " WHERE" +
        " m.writer IN (:userOneId,:userToId)" +
        "AND m.writer IN (:userOneId,:userToId)" +
        " ORDER BY m.timeSent DESC ")
 List<ChatMessage> getExistingChatMessages(@Param("UserOneId")Long userOneId, @Param("userTwoId")Long userTwoId, Pageable pageable);


}
