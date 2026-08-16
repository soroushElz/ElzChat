package com.example.ChatApplication.chat.repositories;

import com.example.ChatApplication.chat.models.ChatMessage;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage,Long> , JpaSpecificationExecutor<ChatMessage> {


 @Query("FROM " +
         "ChatMessage m " +
         " WHERE" +
         " m.destinationId=:channelId" +
         " ORDER BY m.timeSent DESC "
 )
 List<ChatMessage> findChatMessagesByChatId(@Param("channelId") Long channelId, Pageable pageable);

 @Query("SELECT message FROM " +
         " ChatMessage message join MessageDeliveryStatus mds on message.id=mds.message.id" +
         " WHERE message.destinationId=:channelId " +
         " And mds.deliveryStatus=com.example.ChatApplication.Notification.Status.PENDING " +
         " AND mds.recipient.id=:userId " +
         " ORDER BY message.timeSent DESC "
 )
 List<ChatMessage> findUnDeliveredChatMessagesByChatIdAndRecipientId(@Param("channelId") Long channelId,@Param("userId")Long userId);

 @Query("SELECT message FROM " +
         " ChatMessage message join MessageDeliveryStatus mds on message.id=mds.message.id" +
         " WHERE mds.recipient.id=:userId " +
         " And mds.deliveryStatus=com.example.ChatApplication.Notification.Status.PENDING " +
         " ORDER BY message.timeSent DESC "
 )
 List<ChatMessage> findAllPendingMessagesByRecipientId(@Param("userId") Long userId);
}

