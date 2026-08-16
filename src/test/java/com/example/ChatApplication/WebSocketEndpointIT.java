package com.example.ChatApplication;
import com.example.ChatApplication.Notification.*;
import com.example.ChatApplication.Notification.Block.BlockAction;
import com.example.ChatApplication.Notification.Block.BlockNotificationPayload;
import com.example.ChatApplication.Notification.Error.ErrorNotificationPayload;
import com.example.ChatApplication.auth.DTO.AuthenticationResponse;
import com.example.ChatApplication.Role.Role;
import com.example.ChatApplication.Role.RoleRepository;
import com.example.ChatApplication.chat.ChatType;
import com.example.ChatApplication.chat.Dtos.*;
import com.example.ChatApplication.chat.models.*;
import com.example.ChatApplication.chat.repositories.*;
import com.example.ChatApplication.Group.DTO.CreateGroupRequest;
import com.example.ChatApplication.Group.DTO.GroupSummaryResponse;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.dtos.UpdateBlockListRequest;
import com.example.ChatApplication.user.dtos.UserDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.Assertions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatList;
import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class WebSocketEndpointIT {
    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;
    @Autowired
    ChatChannelRepository chatChannelRepository;
    @Autowired
    ChatMessageRepository messageRepository;
    @Autowired
    RoleRepository roleRepository;
    Logger logger = LogManager.getLogger();

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${server.port}")
    private int port;
    private String BASE_URL;
    private HashMap<String, String> jwts = new HashMap<>();
    CompletableFuture<ChatMessageDto> receivedMessage;
    BlockingDeque<NotificationDto> receivedEvent = new LinkedBlockingDeque<>();


    @BeforeEach
    public void init() {
        entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getSchemaManager().truncateMappedObjects();
        Role role = Role.builder()
                .name("USER").build();
        roleRepository.save(role);
        BASE_URL = "ws://localhost:" + port + "/api/v1/ws";
        receivedMessage = new CompletableFuture<ChatMessageDto>();
    }


    @Test
    public void testSendMessage_andRead_bySubscribers() throws ExecutionException, InterruptedException, TimeoutException, JSONException {
        var user1 = createNewUser("user1@gmail.com");
        loginUser(user1.getEmail());
        var user2 = createNewUser("user2@gmail.com");
        loginUser(user2.getEmail());

        var channel = new ChatChannel();
        channel.addUser(user1);
        channel.addUser(user2);
        channel.setChatType(ChatType.PRIVATE_CHAT);
        var savedChannel = chatChannelRepository.save(channel);
        Long channelId = savedChannel.getId();
        StompSession stompSessionOne = establishJwtAuthorizedStompSessionForUser(user1);
        StompSession stompSessionTwo = establishJwtAuthorizedStompSessionForUser(user2);
        stompSessionTwo.subscribe("/user/topic/chat",
                getSubscriptionHandlerForMessageDTOs()
        );

        stompSessionOne.send("/app/chat/" + String.valueOf(channelId), new MessageRequest(channelId, "hello", null));
        Thread.sleep(100);
        stompSessionTwo.send("/app/chat/" + String.valueOf(channelId), new MessageRequest(channelId, "hello4", null));
        List<ChatMessageDto> messagesReceived = receiveNumberOfIncomingMessages(2);
        Long MessageToReply = messagesReceived.get(0).getMessageId();
        stompSessionTwo.send("/app/chat/" + String.valueOf(channelId), new MessageRequest(channelId, "replyMessage", MessageToReply));
        messagesReceived.addAll(receiveNumberOfIncomingMessages(1));
        var replyMessages = messagesReceived.stream()
                .filter(message -> message.getReplyTo() != null).toList().get(0);
        ////Assert Normal Messages Received
        assertThatList(messagesReceived).extracting(ChatMessageDto::getContent).containsExactlyInAnyOrder("hello", "hello4", "replyMessage");
        ////Assert reply happened Successfully
        assertThat(replyMessages).extracting(ChatMessageDto::getReplyTo).isEqualTo(MessageToReply);
     ///user2 doesn't receive messages after being disconnected
        stompSessionTwo.disconnect();
        stompSessionOne.send("/app/chat/" + String.valueOf(channelId), new MessageRequest(channelId, "user1: are you offline?", null));
        Thread.sleep(50);
        ///user2 receives undelivered Messages from chatChannel while he was offline
        var header=new HttpHeaders();
        header.set("Authorization", "Bearer " + jwts.get(user2.getUsername()));
        ResponseEntity<List<ChatMessageDto>> User2pendingMessages = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/chat/messages/pending?channelId="+channelId,
                HttpMethod.GET
                , new HttpEntity<>(header)
                , new ParameterizedTypeReference<>() {
                }
        );
        assertEquals(1, Objects.requireNonNull(User2pendingMessages.getBody()).size());
        assertTrue(User2pendingMessages.getBody().get(0).getContent().contains("user1: are you offline?"));
        ///user1 receive all Undelivered messages
       ///expected:
        List<ChatMessage> undeliveredMessagesToUser1=messageRepository.findAllPendingMessagesByRecipientId(user1.getId());
        header.set("Authorization", "Bearer " + jwts.get(user1.getUsername()));
        ResponseEntity<List<ChatMessageDto>> user1pendingMessagesResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/chat/messages/pending",
                HttpMethod.GET
                , new HttpEntity<>(header)
                , new ParameterizedTypeReference<>() {
                }
        );
        ///actual
        var actualReceivedMessagesByUser1=user1pendingMessagesResponse.getBody();
        assertThatList(actualReceivedMessagesByUser1).extracting(ChatMessageDto::getMessageId)
                .containsExactlyInAnyOrderElementsOf(undeliveredMessagesToUser1.stream().map(ChatMessage::getId).toList()
                );

    }

    @Test
    public void test_sendReaction_removeReaction_userGetUpdate() throws JSONException, ExecutionException, InterruptedException, TimeoutException {
        var user1 = createNewUser("soroush.yz.97@gmail.com");
        var user2 = createNewUser("sogand.yz.97@gmail.com");
        loginUser(user1.getUsername());
        loginUser(user2.getUsername());
        ////create channel
        var chatChannel = new ChatChannel();
        chatChannel.setChatType(ChatType.PRIVATE_CHAT);
        chatChannel.addUser(user1);
        chatChannel.addUser(user2);
        ChatChannel savedChannel = chatChannelRepository.saveAndFlush(chatChannel);
        ////subscribe to event queue
        StompSession stompSessionForUser = establishJwtAuthorizedStompSessionForUser(user2);
        stompSessionForUser.subscribe("/user/queue/events",
                getSubscriptionHandlerForNotification()
        );
        Thread.sleep(200);
        ///create Message in channel
        ChatMessage message = ChatMessage.builder().writer(user1).destinationId(savedChannel.getId())
                .content("hello sogand!").timeSent(LocalDateTime.now()).build();
        var savedMessage = messageRepository.saveAndFlush(message);
        ////test send reaction And receive Ack when Successful
        /////send like by user
        var addLikeRequestResponse = createResponseSendingPostReactionRequest(new ReactionRequest(savedMessage.getId(), ReactionType.LIKE, ReactionAction.ADD), user1);

        assertTrue(addLikeRequestResponse.getStatusCode().is2xxSuccessful());
        assertEquals(addLikeRequestResponse.getBody(), new ReactionAckResponseDto(savedMessage.getId().toString(), new UserDto(user1.getId(), user1.getUsername()), ReactionType.LIKE, ReactionAction.ADD.name()));
        ///Test receiving reaction event
        var actualReaction = receivedEvent.poll();
        assertEquals(actualReaction, new ReactionAckResponseDto(savedMessage.getId().toString(), new UserDto(user1.getId(), user1.getUsername()), ReactionType.LIKE, ReactionAction.ADD.name()));

        ////test remove reaction
        ResponseEntity<ReactionAckResponseDto> deleteReactionResponse = createResponseSendingPostReactionRequest(new ReactionRequest(savedMessage.getId(), ReactionType.LIKE, ReactionAction.REMOVE), user1);
        assertTrue(deleteReactionResponse.getStatusCode().is2xxSuccessful());
        assertEquals(deleteReactionResponse.getBody(), new ReactionAckResponseDto(savedMessage.getId().toString(), new UserDto(user1.getId(), user1.getUsername()), ReactionType.LIKE, ReactionAction.REMOVE.name()));
        /////error response when reaction does not exists
        var header = new HttpHeaders();
        header.set("Authorization", "Bearer " + jwts.get(user1.getUsername()));
        ResponseEntity<Map> reactionDoesNotExistsErrorResponse = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1" + "/message/" + String.valueOf(savedMessage.getId()) + "/reaction"
                , new HttpEntity<>(new ReactionRequest(savedMessage.getId(), ReactionType.LIKE, ReactionAction.REMOVE), header)
                , Map.class
        );
        assertEquals("reaction does not exists", reactionDoesNotExistsErrorResponse.getBody().get("error"));
        /////error response when reaction Already exists by user
        createResponseSendingPostReactionRequest(new ReactionRequest(savedMessage.getId(), ReactionType.FIRE, ReactionAction.ADD), user1);
        ResponseEntity<Map> reactionExistsErrorResponse = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1" + "/message/" + String.valueOf(savedMessage.getId()) + "/reaction"
                , new HttpEntity<>(new ReactionRequest(savedMessage.getId(), ReactionType.LIKE, ReactionAction.ADD), header)
                , Map.class
        );
        assertEquals("user reaction already exists!", reactionExistsErrorResponse.getBody().get("error"));

    }

    @Test
    public void Test_get_BlockedNotification_whenBlocked_getError_sendingToBlocker() throws JSONException, ExecutionException, InterruptedException, TimeoutException {
        ////creat test users and chatChannel and add members
        var user1 = createNewUser("soroush.yz.97@gmail.com");
        var user2 = createNewUser("sogand.yz.97@gmail.com");
        loginUser(user1.getUsername());
        loginUser(user2.getUsername());
        ////user2 start connection and become online
        StompSession stompSessionForUser2 = establishJwtAuthorizedStompSessionForUser(user2);
        ////user2 subscribe to get blocked notifications
        stompSessionForUser2.subscribe("/user/queue/notification/block",
                getSubscriptionHandlerForNotification()
        );
        ///subscribe to error endpoint
        stompSessionForUser2.subscribe("/user/queue/notification/error",
                getSubscriptionHandlerForNotification()
        );
        ///create channel
        var chatChannel = new ChatChannel();
        chatChannel.setChatType(ChatType.PRIVATE_CHAT);
        chatChannel.addUser(user1);
        chatChannel.addUser(user2);
        ChatChannel savedChannel = chatChannelRepository.saveAndFlush(chatChannel);
        ///user1 block user2
        var header = new HttpHeaders();
        header.set("Authorization", "Bearer " + jwts.get(user1.getUsername()));
        ResponseEntity<Void> blockUserResponse = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1" + "/user/updateBlockList"
                , new HttpEntity<>(new UpdateBlockListRequest(Set.of(user2.getId()), null), header)
                , Void.class);
        ////get blocked notification by user1
        NotificationDto blockedNotification = receivedEvent.poll(2, TimeUnit.SECONDS);
        assert blockedNotification != null;
        var blockNotificationPayload = (BlockNotificationPayload) blockedNotification.getPayload();
        assertEquals(blockNotificationPayload.getBlockedBy(), new UserDto(user1.getId(), user1.getUsername()));
        assertEquals(blockNotificationPayload.getBlockedUser(), new UserDto(user2.getId(), user2.getUsername()));
        /////user2 unable to send message and receives error from /user/queue/notification/error endpoint
        stompSessionForUser2.send("/app/chat/" + String.valueOf(savedChannel.getId()), new MessageRequest(savedChannel.getId(), "hello", null));
        NotificationDto UnableToSendError = receivedEvent.poll(2, TimeUnit.SECONDS);
        var errorNotificationPayload = (ErrorNotificationPayload) UnableToSendError.getPayload();
        assertTrue(errorNotificationPayload.getMessage().contains("you are blocked!"));
        ///user1 unblock user2
        ResponseEntity<Void> unBlockUserResponse = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1" + "/user/updateBlockList"
                , new HttpEntity<>(new UpdateBlockListRequest(null, Set.of(user2.getId())), header)
                , Void.class);
        NotificationDto unBlockedNotification = receivedEvent.poll(2, TimeUnit.SECONDS);
        var unBlockedNotificationPayload = (BlockNotificationPayload) unBlockedNotification.getPayload();
        assertEquals(unBlockedNotificationPayload.getAction(), BlockAction.UNBLOCK);
        assertEquals(unBlockedNotificationPayload.getBlockedUser(), new UserDto(user2.getId(), user2.getUsername()));
    }

    /////////test receive notification after user become online
    @Test
    public void Test_user_receives_NotificationCreated_while_recipientWasOffline() throws JSONException {
       //// user was offline since last day
        var user = createNewUser("soroush.yz.97@gmail.com");
        transactionTemplate.executeWithoutResult(status -> {
                    user.setLastOffline(LocalDateTime.now().minusDays(1));
                    entityManager.merge(user);
                }
        );
        var user2 = createNewUser("user2@gmail.com");
        var header = new HttpHeaders();
        ///////user2 block user while user is offline and notification is saved in pending status
        loginUser(user2.getName());
        header.set("Authorization", "Bearer " + jwts.get(user2.getUsername()));
        ResponseEntity<Void> blockUserResponse = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1" + "/user/updateBlockList"
                , new HttpEntity<>(new UpdateBlockListRequest(Set.of(user.getId()), null), header)
                , Void.class);
        assertTrue(blockUserResponse.getStatusCode().is2xxSuccessful());
       //// user retrieve notifications created while user was offline
         header = new HttpHeaders();
        loginUser(user.getName());
        header.set("Authorization", "Bearer " + jwts.get(user.getUsername()));
        ResponseEntity<List<Notification>> pendingNotificationsResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/user/notification/pending",
                HttpMethod.GET
                , new HttpEntity<>(header)
                , new ParameterizedTypeReference<>() {
                }
        );
        logger.info(pendingNotificationsResponse.getBody());
        assertTrue(pendingNotificationsResponse.getStatusCode().is2xxSuccessful());
        assertFalse(Objects.requireNonNull(pendingNotificationsResponse.getBody()).isEmpty());
        List<Notification> notifications=pendingNotificationsResponse.getBody();
        BlockNotificationPayload payload= (BlockNotificationPayload) notifications.get(0).getNotificationPayload();

        assertArrayEquals(
                new Object[]{user.getId(), user2.getId(), user.getId()},
                new Object[]{payload.getBlockedUser().getId(),
                        payload.getBlockedBy().getId(),
                        notifications.get(0).getRecipientId()}
        );
    }


    ////user1 ask for contact(chat list)
    @Test
    public void test_findAllChats_forUser() throws JSONException {
        var user1 = createNewUser("soroush.yz.97@gmail.com");
        loginUser(user1.getUsername());
        List<ChatChannel> chatList = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            var user = createNewUser("user " + i + "@gmail.com");
            var chatChannel = new ChatChannel();
            chatChannel.setChatType(ChatType.PRIVATE_CHAT);
            chatChannel.addUser(user1);
            chatChannel.addUser(user);
            ChatChannel savedChannel = chatChannelRepository.saveAndFlush(chatChannel);
            chatList.add(savedChannel);
        }
        var header = new HttpHeaders();
        header.set("Authorization", "Bearer " + jwts.get(user1.getUsername()));
        ResponseEntity<List<ChatChannelDto>> user1ContactListResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/user/contacts",
                HttpMethod.GET
                , new HttpEntity<>(header)
                , new ParameterizedTypeReference<>() {
                }
        );
        List<ChatChannelDto> actualContactList = user1ContactListResponse.getBody();
        assertThatList(actualContactList).extracting(ChatChannelDto::getChannelId)
                .containsExactlyInAnyOrderElementsOf(chatList.stream().map(ChatChannel::getId).toList());

    }

    ////user2 forward message from source to destination user3 receives message in destinationChat
    @Test
    public void test_forwardMessages() throws ExecutionException, InterruptedException, TimeoutException, JSONException {
        var user1 = createNewUser("user1@gmail.com");
        var user2 = createNewUser("user2@gmail.com");
        loginUser(user2.getEmail());
        var user3 = createNewUser("user3@gmail.com");
        loginUser(user3.getEmail());
        /////create source and destination channel
        var sourceChannel = new ChatChannel();
        sourceChannel.addUser(user1);
        sourceChannel.addUser(user2);
        sourceChannel.setChatType(ChatType.PRIVATE_CHAT);
        var destinationChannel = new ChatChannel();
        destinationChannel.addUser(user2);
        destinationChannel.addUser(user3);
        destinationChannel.setChatType(ChatType.PRIVATE_CHAT);
        var savedSourceChannel = chatChannelRepository.saveAndFlush(sourceChannel);
        var savedDestinationChannel = chatChannelRepository.saveAndFlush(destinationChannel);
        ChatMessage messageToForward = ChatMessage.builder().destinationId(sourceChannel.getId())
                .writer(user1).timeSent(LocalDateTime.now()).content("hello from user1").build();
        var savedMessageToForward = messageRepository.saveAndFlush(messageToForward);
        StompSession stompSessionUserTwo = establishJwtAuthorizedStompSessionForUser(user2);
        StompSession stompSessionUserThree = establishJwtAuthorizedStompSessionForUser(user3);
        stompSessionUserThree.subscribe("/user/topic/chat",
                getSubscriptionHandlerForMessageDTOs()
        );

        stompSessionUserTwo.send("/app/chat/" + savedDestinationChannel.getId() + "/forward", new ForwardMessageRequest(List.of(savedMessageToForward.getId()),
                savedSourceChannel.getId(),
                savedDestinationChannel.getId()));
        Thread.sleep(100);

        ///user3 inbox
        List<ChatMessageDto> messagesReceived = receiveNumberOfIncomingMessages(1);
        var forwardedMessage = messagesReceived.get(0);
        assertTrue(forwardedMessage.isForwarded());
        assert forwardedMessage.getOriginalSender() != null;
        assertEquals(forwardedMessage.getOriginalSender().getId(), messageToForward.getWriter().getId());
        assertEquals(forwardedMessage.getSenderDto().getId(), user2.getId());

    }

    @Test
    public void test_SearchMessages_byFilter() throws JSONException {
        ///create users and Chat channel and fill it with random messages
        var user1 = createNewUser("user1@gmail.com");
        var user2 = createNewUser("user2@gmail.com");
        loginUser(user1.getEmail());
        var chatChannel = new ChatChannel();
        chatChannel.addUser(user1);
        chatChannel.addUser(user2);
        chatChannel.setChatType(ChatType.PRIVATE_CHAT);
        String[] sampleTexts = {"text1...", "text2...", "text3...", "text4..."};
        var savedChatChannel = chatChannelRepository.saveAndFlush(chatChannel);
        List<ChatMessage> testMessages = Stream.generate(() ->
                        ChatMessage.builder()
                                .writer(List.of(user1, user2).get(ThreadLocalRandom.current().nextInt(2)))
                                .destinationId(savedChatChannel.getId())
                                .content(sampleTexts[RandomGenerator.getDefault().nextInt(sampleTexts.length)])
                                .timeSent(LocalDateTime.now().minusHours(RandomGenerator.getDefault().nextLong(120)))
                                .build()
                )
                .limit(100).toList();
        var savedMessages = messageRepository.saveAllAndFlush(testMessages);
        ////send search chat requests
        var header = new HttpHeaders();
        header.set("Authorization", "Bearer " + jwts.get(user1.getUsername()));
        String keyword = sampleTexts[RandomGenerator.getDefault().nextInt(sampleTexts.length)];
        Long Writer_id = List.of(user1.getId(), user2.getId()).get(RandomGenerator.getDefault().nextInt(2));
        LocalDate duringDateSearch = LocalDateTime.now().minusHours(RandomGenerator.getDefault().nextLong(120)).toLocalDate();
        var searchFilters = List.of(new SearchFilters(FieldFilter.CONTENT, keyword, Operation.LIKE)
                , new SearchFilters(FieldFilter.WRITER_ID, Writer_id.toString(), Operation.EQ)
                , new SearchFilters(FieldFilter.TIME_SENT, duringDateSearch.toString(), Operation.DURING)
        );

        ResponseEntity<List<ChatMessageDto>> searchChatResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/chat/" + chatChannel.getId() + "/search",
                HttpMethod.POST, new HttpEntity<>(searchFilters, header), new ParameterizedTypeReference<>() {
                }
        );
        logger.info("search result for keyword:{} writer_id:{} duringDay:{}", keyword, Writer_id, duringDateSearch);
        var expectedMessages = savedMessages.stream()
                .filter(message -> message.getTimeSent().isAfter(duringDateSearch.atStartOfDay())
                        && message.getTimeSent().isBefore(duringDateSearch.plusDays(1).atStartOfDay())
                        && Objects.equals(Writer_id, message.getWriter().getId())
                        && message.getContent().contains(keyword))
                .toList();
        //////test search result
        Assertions.assertThatList(searchChatResponse.getBody())
                  .extracting(ChatMessageDto::getMessageId)
                  .containsExactlyInAnyOrderElementsOf(expectedMessages.stream().map(ChatMessage::getId).toList());

        ////////user3 can't search because it is not chat member
        var user3 = createNewUser("user3@gmail.com");
        loginUser(user3.getEmail());
        header = new HttpHeaders();
        header.set("Authorization", "Bearer " + jwts.get(user3.getUsername()));
        ResponseEntity<String> user3SearchChatResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/chat/" + chatChannel.getId() + "/search",
                HttpMethod.POST
                , new HttpEntity<>(searchFilters, header)
                , new ParameterizedTypeReference<>() { }
        );
        assertTrue(user3SearchChatResponse.getStatusCode().isSameCodeAs(HttpStatusCode.valueOf(403)));
    }


    @Test
    public void test_createGroup_leaveGroup_deleteGroup() throws JSONException, InterruptedException {
        ///create group Members
        User admin=createNewUser("user1@gmail.com");
        loginUser(admin.getName());
        User member2=createNewUser("member2@gmail.com");
        User member3=createNewUser("member3@gmail.com");
       ////create group
        var  header= new HttpHeaders();
        header.set("Authorization", "Bearer " + jwts.get(admin.getUsername()));
        ResponseEntity<GroupSummaryResponse> createGroupResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/group/create",
                HttpMethod.POST
                , new HttpEntity<>(new CreateGroupRequest("newGroup",List.of(member2.getId(), member3.getId())), header)
                , new ParameterizedTypeReference<>() {}
        );
        assertTrue(createGroupResponse.getStatusCode().is2xxSuccessful());
        var createdGroupSummary=createGroupResponse.getBody();
        assert createdGroupSummary != null;
        assertEquals(createdGroupSummary.admin(),new UserDto(admin.getId(), admin.getName()));
        assertThatList(createdGroupSummary.members()).extracting(UserDto::getId)
                .containsExactlyInAnyOrderElementsOf(List.of(admin.getId(), member3.getId(),member2.getId()));
       ////////member2 leave the group
        header.remove("Authorization");
        loginUser(member2.getName());
        header.set("Authorization", "Bearer " + jwts.get(member2.getUsername()));
        ResponseEntity<String> leaveGroupResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/group/"+createdGroupSummary.groupId()+"/leave",
                HttpMethod.PUT
                , new HttpEntity<>(header)
                , new ParameterizedTypeReference<>() {}
        );
        assertTrue(leaveGroupResponse.getStatusCode().is2xxSuccessful());
        logger.info(leaveGroupResponse.getBody());
        ////get groupSummary by Admin and assert member2 left
        header.set("Authorization", "Bearer " + jwts.get(admin.getUsername()));
        ResponseEntity<GroupSummaryResponse> groupSummaryResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/group/"+createdGroupSummary.groupId(),
                HttpMethod.GET
                , new HttpEntity<>(header)
                , new ParameterizedTypeReference<>() {}
        );
        assertFalse(Objects.requireNonNull(groupSummaryResponse.getBody()).members().stream().anyMatch(member-> Objects.equals(member.getId(), member2.getId())));
        ////admin deletes group
        ResponseEntity<Void> deleteGroupResponse = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/v1" + "/group/"+createdGroupSummary.groupId(),
                HttpMethod.DELETE
                , new HttpEntity<>(header)
                , new ParameterizedTypeReference<>() {}
        );
        assertTrue(deleteGroupResponse.getStatusCode().is2xxSuccessful());

    }


    ////test if only users can connect
    @Test
    public void sendingCONNECTRequest_withJWT() throws ExecutionException, InterruptedException, TimeoutException, JSONException {
        var user = createNewUser("soroush.yz.97@gmail.com");
        loginUser(user.getEmail());
        StompSession stompSession = establishJwtAuthorizedStompSessionForUser(user);
        assertTrue(stompSession.isConnected());
    }


    @Test
    public void testEstablishPrivateChatChannel() throws JSONException {
        var user1 = createNewUser("soroush.yz.97@gmail.com");
        loginUser(user1.getUsername());
        var user2 = createNewUser("soheil.yz.97@gmail.com");

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.set("Authorization", "Bearer " + jwts.get(user1.getUsername()));

        ResponseEntity<EstablishedPrivateChannelDto> response = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1" + "/chat/newChannel"
                , new HttpEntity<>(new PrivateChatInitializationDto(user1.getId(), user2.getId()), headers)
                , EstablishedPrivateChannelDto.class
        );
        assertNotNull(response.getBody());
    }


    private ResponseEntity<ReactionAckResponseDto> createResponseSendingPostReactionRequest(ReactionRequest request, User user) {
        var header = new HttpHeaders();
        header.set("Authorization", "Bearer " + jwts.get(user.getUsername()));
        return testRestTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1" + "/message/" + String.valueOf(request.messageId()) + "/reaction"
                , new HttpEntity<>(request, header)
                , ReactionAckResponseDto.class
        );
    }

    private StompFrameHandler getSubscriptionHandlerForMessageDTOs() {
        return new StompFrameHandler() {
            @Override
            @Nonnull
            public Type getPayloadType(StompHeaders headers) {
                return ChatMessageDto.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                ChatMessageDto msg = (ChatMessageDto) payload;
                receivedMessage.complete(msg);
                logger.info(msg.toString() + ": received by receiver user");
            }
        };
    }

    private StompFrameHandler getSubscriptionHandlerForNotification() {
        return new StompFrameHandler() {
            @Nonnull
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return Notification.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                receivedEvent.add((NotificationDto) payload);

            }


        };
    }


    private StompSession establishJwtAuthorizedStompSessionForUser(User user) throws ExecutionException, InterruptedException, TimeoutException {
        WebSocketStompClient stompClient = createStompClient();
        var stompHeader = new StompHeaders();
        stompHeader.set("Authorization", "Bearer " + jwts.get(user.getUsername()));
        return stompClient.connectAsync(BASE_URL, new WebSocketHttpHeaders(), stompHeader, new StompSessionHandlerAdapter() {
            @Override
            public void handleException(StompSession session, StompCommand command,
                                        StompHeaders headers, byte[] payload, Throwable exception) {
                System.err.println("   Command: " + command);
                System.err.println("   Headers: " + headers);
                System.err.println("   Payload: " + (payload != null ? new String(payload) : "null"));
                System.err.println("   Exception: ");
                exception.printStackTrace();
            }
        }).get(1, TimeUnit.SECONDS);
    }


    private List<ChatMessageDto> receiveNumberOfIncomingMessages(int n) {
        List<ChatMessageDto> messagesReceived = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            receivedMessage.thenAccept(m -> {
                messagesReceived.add((ChatMessageDto) m);
            }).join();

            receivedMessage = new CompletableFuture<>();
        }
        return messagesReceived;
    }

    private WebSocketStompClient createStompClient() {
        WebSocketStompClient client = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        var converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(mapper);
        client.setMessageConverter(converter);
        return client;
    }

    private List<Transport> createTransportClient() {
        return List.of(new WebSocketTransport(new StandardWebSocketClient()));
    }

    private User createNewUser(String mail) {
        return transactionTemplate.execute(transactionStatus -> {
            Role role = entityManager
                    .createQuery("from Role r where r.name= :n ", Role.class)
                    .setParameter("n", "USER").getSingleResult();

            var user = User.builder()
                    .email(mail)
                    .password(new BCryptPasswordEncoder().encode("12345678"))
                    .roles(List.of(role))
                    .build();

            entityManager.persist(user);

            return user;
        });
    }

    public void loginUser(String mail) throws JSONException {
        var jsonAuthenticationRequest = new JSONObject();
        jsonAuthenticationRequest.put("email", mail);
        jsonAuthenticationRequest.put("password", "12345678");
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<AuthenticationResponse> response = testRestTemplate.postForEntity("http://localhost:" + port + "/api/v1" + "/auth/authenticate",
                new HttpEntity<String>(jsonAuthenticationRequest.toString(), headers), AuthenticationResponse.class);

        jwts.put(mail, response.getBody().getAccessToken());

    }
}


