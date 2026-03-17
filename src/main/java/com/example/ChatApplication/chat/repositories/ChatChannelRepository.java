package com.example.ChatApplication.chat.repositories;

import com.example.ChatApplication.chat.models.ChatChannel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatChannelRepository extends JpaRepository<ChatChannel,Long> {

   @Query(("FROM " +
           "ChatChannel c" +
           " WHERE" +
           " c.userOne.id IN (:userOneId,:userTwoId)" +
           " AND" +
           " c.userTwo.id IN (:userTwoId,:userTwoId)"))
    List<ChatChannel> findExistingChannel(@Param("userOneId")Long userOneId, @Param("userIdTwo") Long userIdTwo);


   @Query("FROM " +
           "ChatChannel c" +
           " WHERE " +
           "c.id=:channelId"
           )
   ChatChannel getChannelDetail(@Param("channelId")Long channelId);

   @Query("" +
           "" +
           "" +
           "")
    List<ChatChannel> getChatsListByUserId(Long id);
}
