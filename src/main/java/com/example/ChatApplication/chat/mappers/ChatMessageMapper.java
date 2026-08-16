package com.example.ChatApplication.chat.mappers;

import com.example.ChatApplication.chat.Dtos.ChatMessageDto;
import com.example.ChatApplication.chat.models.ChatMessage;
import com.example.ChatApplication.chat.models.ReactionAggregate;
import com.example.ChatApplication.chat.services.ChatService;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.dtos.UserDto;
import jakarta.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChatMessageMapper {

   @Autowired
    public ChatMessageMapper(ChatService chatService, EntityManagerFactory entityManagerFactory) {
        this.chatService = chatService;
        this.emf=entityManagerFactory;
    }

    ChatService chatService;
   EntityManagerFactory emf;

    public  ChatMessageDto  mapMessagesToMessageDto(ChatMessage m) {
        ChatMessageDto.ChatMessageDtoBuilder DtoBuilder = ChatMessageDto.builder().messageId(m.getId())
                     .senderDto(new UserDto(m.getWriter().getId(), m.getWriter().getUsername()))
                .destinationChannelId(m.getDestinationId())
                     .content(m.getContent())
                .timeSent(m.getTimeSent());

        if(m.isForwarded()) {
            UserDto originalSender = getOriginalSender(m);
            DtoBuilder.forwarded(true).originalSender(originalSender);
        }

        List<ReactionAggregate> reactions=getReactions(m);
       if(!reactions.isEmpty())
           DtoBuilder.reactions(reactions);

        if (m.getReplyTo()==null){
                return  DtoBuilder.build();
            }
           return DtoBuilder.
                   ReplyTo(m.getReplyTo().getId())
                   .build();
    }

    private List<ReactionAggregate> getReactions(ChatMessage m) {
        List<ReactionAggregate> reactions;
        try (var em = emf.createEntityManager()) {
            var transaction = em.getTransaction();
            try {
                transaction.begin();
                TypedQuery<ReactionAggregate> query = em.createQuery("select r from ReactionAggregate r where r.id.messageId=:MessageId",ReactionAggregate.class)
                        .setParameter("MessageId", m.getId()).setHint("org.hibernate.readOnly", true);

                reactions = query.getResultList();
                transaction.commit();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return reactions;
    }

    private UserDto getOriginalSender(ChatMessage m) {
        UserDto originalSender;
        try (var em=emf.createEntityManager()) {
          var transaction= em.getTransaction();
          try {
              transaction.begin();
              TypedQuery<UserDto> getOriginalSenderQuery = em.createQuery("SELECT  new com.example.ChatApplication.user.dtos.UserDto(user.id,user.email) " +
                      "from User user ,ForwardInfo forward " +
                      " WHERE user.id = forward.originalSenderId "+
                      " And forward.forwardedMessageId=:forwardedMessageId ", UserDto.class).setHint("org.hibernate.readOnly", true);
              originalSender=  getOriginalSenderQuery.setParameter("forwardedMessageId", m.getId()).getSingleResult();

              transaction.commit();
          } catch (Exception e) {
              throw new RuntimeException(e);
          }

        }
        return originalSender;
    }


    public   ChatMessage mapMessageDtoToMessage(ChatMessageDto chatMessageDto) {

      if(chatMessageDto.getReplyTo()==null)
        return new ChatMessage(new User(chatMessageDto.getSenderDto().getId(),chatMessageDto.getSenderDto().getEmail())
                                   ,chatMessageDto.getDestinationChannelId()
                                      ,chatMessageDto.getContent()
                                        ,chatMessageDto.getTimeSent()
                                  );

      return new  ChatMessage(new User(chatMessageDto.getSenderDto().getId())
              ,chatMessageDto.getDestinationChannelId()
              ,chatMessageDto.getContent(),
              new ChatMessage(chatMessageDto.getReplyTo())
              ,chatMessageDto.getTimeSent()
      );

    }





}
