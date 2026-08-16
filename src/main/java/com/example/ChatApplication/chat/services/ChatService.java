package com.example.ChatApplication.chat.services;

import com.example.ChatApplication.Exception.ChannelNotFoundException;
import com.example.ChatApplication.Exception.IsSameUserException;
import com.example.ChatApplication.Exception.OperationNotPermittedException;
import com.example.ChatApplication.Notification.Status;
import com.example.ChatApplication.chat.Dtos.*;
import com.example.ChatApplication.chat.mappers.ChatMessageMapper;
import com.example.ChatApplication.chat.models.*;
import com.example.ChatApplication.chat.repositories.*;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.dtos.UserDto;
import com.example.ChatApplication.websocket.SubscriptionCheckInterceptor;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.example.ChatApplication.user.services.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;

import static com.example.ChatApplication.chat.models.ReactionAction.ADD;

@Service
public class ChatService {


    @Autowired
    private ChatMessageRepository chatMessageRepository;
    @Autowired
    private UserService userService;
    @Autowired
    SubscriptionCheckInterceptor checkInterceptor;
    @Autowired
    private ChatChannelRepository chatChannelRepository;
   @Autowired
    private SimpMessagingTemplate template;
   @Autowired
    ChatMessageMapper messageMapper;
   @Autowired
   ForwardInfoRepository forwardInfoRepo;
   @Autowired
   MessageReactionRepository reactionRepository;
   @Autowired
    ReactionAggregateRepository aggregateRepository;
   @Autowired
   TransactionTemplate transactionTemplate;
   @PersistenceContext
    EntityManager entityManager;



    public  boolean isUserMember(long channelId, User user) {
        List<UserDto> members=chatChannelRepository.getMembersByChatId(channelId);
        return members.stream().anyMatch(userDto -> userDto.getId().equals(user.getId()));
    }

    @Transactional
    public void SubmitChatMessage(ChatMessageDto chatMessageDto){
        Optional<ChatChannel> chatChannel=chatChannelRepository.findById(chatMessageDto.getDestinationChannelId());
       if(chatChannel.isEmpty())
           throw new ChannelNotFoundException("no chat with id:"+chatMessageDto.getDestinationChannelId()+" found");


        ChatMessage chatMessage=messageMapper.mapMessageDtoToMessage(chatMessageDto);
        ChatMessage newMessage =chatMessageRepository.save(chatMessage);
        sendMessageToReceivers(newMessage);
    }

    @Transactional
    ///create delivered vs pending status
    protected void sendMessageToReceivers(ChatMessage chatMessage) {
        List<UserDto> members = new ArrayList<>(chatChannelRepository.getMembersByChatId(chatMessage.getDestinationId()));
        Map<String, Object> header = new HashMap<>();
        header.put("event-type","ChatMessage");
        for(UserDto receiver: members){
            if(userService.isUserSubscribed(receiver.getEmail(), "/user/topic/chat")){
                members.forEach(user ->
                        template.convertAndSendToUser(user.getEmail(),"/topic/chat", messageMapper.mapMessagesToMessageDto(chatMessage),header)
                );
                upsertMessageDeliveryStatus(chatMessage,receiver, Status.DELIVERED);
            }else {  ///user is not available
                upsertMessageDeliveryStatus(chatMessage,receiver,Status.PENDING);
            }

        }


    }
    @Transactional
    protected void upsertMessageDeliveryStatus(ChatMessage chatMessage, UserDto receiver, Status changeToStatus) {
        try{
            MessageDeliveryStatus existingStatus= entityManager.createQuery("select md from MessageDeliveryStatus md " +
                    "where md.recipient.id=:recipientId AND " +
                    "md.message.id=:messageId AND " +
                    "md.deliveryStatus=com.example.ChatApplication.Notification.Status.PENDING",MessageDeliveryStatus.class)
                 .setParameter("recipientId",receiver.getId())
                        .setParameter("messageId",chatMessage.getId())
                .getSingleResult();
            existingStatus.setDeliveryStatus(Status.DELIVERED);
            entityManager.persist(existingStatus);
        }catch (NoResultException exp){
            MessageDeliveryStatus messageDeliveryStatus=new MessageDeliveryStatus(
                    changeToStatus,
                    entityManager.getReference(User.class,receiver.getId()),
                    entityManager.getReference(ChatMessage.class,chatMessage.getId()));
           entityManager.persist(messageDeliveryStatus);
        }
    }

    public Long establishPrivateChatSession(PrivateChatInitializationDto chatChannelInitializationDto)
        throws IsSameUserException,UsernameNotFoundException ,BeansException {

        Long channelId=getExistingChannel(chatChannelInitializationDto);
        return channelId!=null ? channelId
                                : createNewPrivateChatChannel(chatChannelInitializationDto);
    }

    private Long createNewPrivateChatChannel(PrivateChatInitializationDto privateChatInitializationDto)
            throws BeansException, UsernameNotFoundException{
        ChatChannel channel=new ChatChannel();
        channel.addUser(userService.getUser(privateChatInitializationDto.getUserOneId()));
        channel.addUser(userService.getUser(privateChatInitializationDto.getUserTwoId()));
        ChatChannel savedChannel=chatChannelRepository.save(channel);

        return savedChannel.getId();
    }

    private Long getExistingChannel(PrivateChatInitializationDto chatChannelInitializationDto) {
        List<ChatChannel> chatChannel= chatChannelRepository.findPrivateChatChannel(
                      chatChannelInitializationDto.getUserOneId()
                        ,chatChannelInitializationDto.getUserTwoId());
        return (chatChannel!=null && !chatChannel.isEmpty()) ? chatChannel.get(0).getId() : null;
    }

    @Transactional
    public List<ChatMessageDto> getAllChatMessagesByChannelId(Long channelId, Pageable pageable) {
        Optional<ChatChannel> channel=chatChannelRepository.findById(channelId);
        if(channel.isEmpty()) throw new ChannelNotFoundException("chat does not exists!");

        var user=(User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ChatMessage> chatMessages= chatMessageRepository.findChatMessagesByChatId(channelId,pageable);
       ///////change status of messages to delivered
        chatMessages.forEach(message->
                upsertMessageDeliveryStatus(message,new UserDto(user.getId(), user.getUsername()),Status.DELIVERED));

        return chatMessages.stream()
                .map(messageMapper::mapMessagesToMessageDto)
                 .toList();

    }
      @Transactional
      public List<ChatMessageDto> getAllUndeliveredChatMessagesByRecipient(User recipient){
       List<ChatMessage> unDeliveredMessages= chatMessageRepository.findAllPendingMessagesByRecipientId(recipient.getId());
          unDeliveredMessages.forEach(message->
                  upsertMessageDeliveryStatus(message,new UserDto(recipient.getId(), recipient.getUsername()),Status.DELIVERED)
          );

          return unDeliveredMessages.stream()
                  .map(messageMapper::mapMessagesToMessageDto)
                  .toList();
      }

    @Transactional
    public List<ChatMessageDto> getUserUnseenChatMessagesByChannelId(Long channelId) {
        Optional<ChatChannel> channel=chatChannelRepository.findById(channelId);
        if(channel.isEmpty()) throw new ChannelNotFoundException("chat does not exists!");
        var user=(User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<ChatMessage> chatMessages= chatMessageRepository.findUnDeliveredChatMessagesByChatIdAndRecipientId(channelId, user.getId());
        chatMessages.forEach(message->
                upsertMessageDeliveryStatus(message,new UserDto(user.getId(), user.getUsername()),Status.DELIVERED)
        );

            return chatMessages.stream()
                .map(messageMapper::mapMessagesToMessageDto)
                .toList();
    }


    @Transactional
    public void SubmitMessageReply(ChatMessageDto chatMessage) {
        Optional<ChatMessage> message = chatMessageRepository.findById(chatMessage.getReplyTo());
         if(message.isEmpty()){
             return;
         }
        if( ! message.get().getDestinationId().equals(chatMessage.getDestinationChannelId()))
            throw new OperationNotPermittedException("message is not in this channel");

        SubmitChatMessage(chatMessage);
    }


    public void forwardMessage(ForwardMessageRequest forwardMessagesRequest,User user) {

        if(!validateForward(user,forwardMessagesRequest))
            return;

        forwardMessagesRequest .messageIds().forEach(originalMessageId-> {
                 ChatMessage forwarded=  transactionTemplate.execute(status -> {
                        var originalMessage = chatMessageRepository.findById(originalMessageId).orElse(null);
                        assert originalMessage != null;

                        var forwardedMessage = ChatMessage.builder()
                                .destinationId(forwardMessagesRequest.destinationChatId())
                                .content(originalMessage.getContent())
                                .forwarded(true)
                                .timeSent( LocalDateTime.now())
                                .writer(user).build();
                        var savedForwarded = chatMessageRepository.saveAndFlush(forwardedMessage);

                        var forwardInfo = ForwardInfo.builder()
                                .forwardedMessageId(savedForwarded.getId())
                                .senderId(user.getId())
                                .originalMessageId(originalMessage.getId())
                                .originalSenderId(originalMessage.getWriter().getId())
                                .originalMessageTextSnapShot(originalMessage.getContent()).build();
                        forwardInfoRepo.save(forwardInfo);
                   return savedForwarded;
                    });


                    assert forwarded != null;
                    sendMessageToReceivers(forwarded);

                }
                );
    }

    private boolean validateForward(User user, ForwardMessageRequest forwardMessageRequest) {

       for(Long messageId :forwardMessageRequest.messageIds()) {
           var messageOptional = chatMessageRepository.findById(messageId);
           if (messageOptional.isEmpty()) {
               return false;
           }
           var message=messageOptional.get();


           if (checkInterceptor.checkIsAllowed(String.valueOf(message.getDestinationId()), user)
                   && checkInterceptor.checkIsAllowed(String.valueOf(forwardMessageRequest.destinationChatId()), user)
           ) return true;
       }
        return false;
    }

    @Transactional
    public ReactionAckResponseDto makeReaction(ReactionRequest reactionRequest, User user) throws RuntimeException {
        var message=chatMessageRepository.findById(reactionRequest.messageId()).orElseThrow(()-> new OperationNotPermittedException("message Not found"));
        var reaction=reactionRepository.findMessageReactionByMessageIdAndUserId(reactionRequest.messageId(),user.getId());
        switch (reactionRequest.action()){
            case ADD -> {
                if (reaction.isPresent()){
                    throw new OperationNotPermittedException("user reaction already exists!");
                }
                var newReaction=new MessageReaction(null,user,message,reactionRequest.reactionType(), Instant.now());
                reactionRepository.save(newReaction);
            }
            case REMOVE -> {
                if(reaction.filter(r->r.getType().equals(reactionRequest.reactionType())).isEmpty())
                    throw new MissingResourceException("reaction does not exists",MessageReaction.class.getName(), String.valueOf(reactionRequest.messageId()));
                reactionRepository.delete(reaction.get());
            }
        }
            updateMessageReactionAggregate(reactionRequest.messageId(),reactionRequest.action(),reactionRequest.reactionType());

            var reactionDto =ReactionAckResponseDto.builder()
                            .reactionType(reactionRequest.reactionType())
                            .Action(reactionRequest.action().name())
                            .messageId(String.valueOf(reactionRequest.messageId()))
                            .user(new UserDto(user.getId(),user.getUsername()))
                            .build();

            chatChannelRepository.findById(message.getDestinationId()).get().getChatMembers().forEach(member->
                sendReactionEventToUser(member, reactionDto)
                );

        return reactionDto;

    }

    private void sendReactionEventToUser(User user, ReactionAckResponseDto reactionDto) {
        Map<String, Object> header = new HashMap<>();
        header.put("event-type","reaction");
        template.convertAndSendToUser(user.getUsername(),"/queue/events", reactionDto,header);
    }


    private void updateMessageReactionAggregate(Long messageId, ReactionAction action, ReactionType reactionType) {
        ReactionAggregate.ReactionCompositeKey key=new ReactionAggregate.ReactionCompositeKey(messageId,reactionType);
        var aggregate= aggregateRepository.findById(key);
        aggregate.ifPresentOrElse(agg->{
           if ((action.equals(ADD))) {
               agg.addCount();
      } else {agg.subtractCount();
           }
          aggregateRepository.save(agg);
       },()->
          aggregateRepository.save(new ReactionAggregate(key))
       );

    }

    public List<ChatMessageDto> searchChat(String channelId, List<SearchFilters> filters,Pageable pageable) {

         boolean exists=chatChannelRepository.existsById(Long.valueOf(channelId));
         if(!exists) throw new ChannelNotFoundException("channel with id:"+channelId+" does not exists!");

         Specification<ChatMessage> chatSpec = getChatMessageSpecification(filters);
        Specification<ChatMessage> specification=chatSpec.and( (root, query, criteriaBuilder) ->
                 criteriaBuilder.equal(root.get(ChatMessage_.DESTINATION_ID), Long.valueOf(channelId)));

         Page<ChatMessage> chatMessages=chatMessageRepository.findAll(specification,pageable);
         ////change message delivery status
        var recipientUser=userService.getUser(SecurityContextHolder.getContext());
        chatMessages.forEach(chatMessage ->
                upsertMessageDeliveryStatus(chatMessage,new UserDto(recipientUser.getId(),recipientUser.getUsername()),Status.DELIVERED)
        );
        return chatMessages.stream()
                .map(messageMapper::mapMessagesToMessageDto)
                .sorted(Comparator.comparing(ChatMessageDto::getTimeSent))
                .toList();

    }


    public static  Specification<ChatMessage> getChatMessageSpecification(List<SearchFilters> filters) {
        return (Root<ChatMessage> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
             List<Predicate> predicateList=  filters.stream().map( filter ->
                      filter.getOperation().buildPredicate( criteriaBuilder, root, filter)
                      )
                      .toList();

            if (!predicateList.isEmpty()) return criteriaBuilder.and( predicateList.toArray(new Predicate[0]));
            return criteriaBuilder.conjunction();
        };
    }

}
