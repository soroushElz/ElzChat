package com.example.ChatApplication.chat.repositories;

import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.user.dtos.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatChannelRepository extends JpaRepository<ChatChannel,Long>{

   @Query("FROM " +
           "ChatChannel c join c.chatMembers m" +
           " WHERE" +
           " c.chatType = com.example.ChatApplication.chat.ChatType.PRIVATE_CHAT " +
           "And m.id IN (:userOneId,:userTwoId) " +
           "group by c.id" +
           " HAVING count (distinct m.id)=2"
           )
    List<ChatChannel> findPrivateChatChannel(@Param("userOneId")Long userOneId, @Param("userTwoId") Long userIdTwo);


    @Query("select new com.example.ChatApplication.user.dtos.UserDto(u.id,u.email) FROM " +
            "ChatChannel c join c.chatMembers u" +
            " WHERE" +
            " c.id = :channel_id"
    )
    List<UserDto> getMembersByChatId(@Param("channel_id") long ld);
}
