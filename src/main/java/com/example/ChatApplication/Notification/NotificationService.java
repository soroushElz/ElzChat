package com.example.ChatApplication.Notification;

import com.example.ChatApplication.Notification.Block.BlockAction;
import com.example.ChatApplication.Notification.Block.BlockNotificationPayload;
import com.example.ChatApplication.Notification.Error.ErrorNotificationPayload;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.dtos.UserDto;
import com.example.ChatApplication.user.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    @Autowired
    private SimpMessagingTemplate template;
    @Autowired
    NotificationFactory notificationFactory;
    @Autowired
    NotificationRepository notificationRepository;
    @Autowired
    UserService userService;
    @Transactional
    public void sendBlockNotification(User userBlocking, User userBlocked, BlockAction action){

        NotificationDto blockNotification= notificationFactory.create(
                new BlockNotificationPayload(new UserDto(userBlocking.getId(),userBlocking.getUsername())
                                               ,new UserDto(userBlocked.getId(),userBlocked.getUsername())
                                               ,Instant.now()
                                               ,action)
                ,new UserDto(userBlocked.getId(),userBlocked.getUsername())
        );

        if(userService.isUserSubscribed(userBlocked.getUsername(),"/user/queue/notification/block")){
            Map<String, Object> header = new HashMap<>();
            header.put("notification-type","block");
            template.convertAndSendToUser(blockNotification.getRecipient().getEmail(),"/queue/notification/block", blockNotification,header);
            persistNotification(blockNotification,Status.DELIVERED);
        }  else {
            persistNotification(blockNotification,Status.PENDING);
        }

    }


    @Transactional
    public List<Notification> receivePendingNotificationByRecipient(User user){
        List<Notification> notificationList=notificationRepository.findByStatusAndRecipientIdAndCreatedDateAfter(Status.PENDING,user.getId(),user.getLastOffline());
        notificationList.forEach(notification -> notification.setStatus(Status.DELIVERED));
        notificationList.forEach(notification->notification.setTimeDelivered(LocalDateTime.now()));
        return notificationList;
   }
    private void persistNotification(NotificationDto notification, Status status) {
        Notification notificationEntity= Notification.builder()
                .createdDate(LocalDateTime.now()).recipientId(notification.getRecipient().getId())
                 .notificationType(NotificationType.BLOCKED)
                  .notificationPayload(notification.getPayload())
                  .status(status)
                   .timeDelivered((status==Status.DELIVERED)? LocalDateTime.now():null)
               .build();
        notificationRepository.save(notificationEntity);
    }

//    @Transactional
//    public void sendGroupEventNotification(){
//        Notification blockNotification= notificationFactory.create(
//                new BlockNotificationPayload(new UserDto(userBlocking.getId(),userBlocking.getUsername())
//                        ,new UserDto(userBlocked.getId(),userBlocked.getUsername())
//                        ,Instant.now()
//                        ,action)
//                ,new UserDto(userBlocked.getId(),userBlocked.getUsername())
//        );
//
//        if(userBlocked.isOnline()){
//            Map<String, Object> header = new HashMap<>();
//            header.put("notification-type","block");
//            template.convertAndSendToUser(blockNotification.getRecipient().getEmail(),"/queue/notification/block", blockNotification,header);
//            persistNotification(blockNotification,Status.DELIVERED);
//        }  else {
//            persistNotification(blockNotification,Status.PENDING);
//        }
//    }
    @Transactional
    public void sendAlertToUser(String alarmMessage, User user) {
        Map<String, Object> header = new HashMap<>();
        header.put("notification-type","alarm");
        NotificationDto errorNotification = notificationFactory.create(
                new ErrorNotificationPayload(alarmMessage,Instant.now())
                        ,new UserDto(user.getId(),user.getUsername())
        );
        template.convertAndSendToUser(errorNotification.getRecipient().getEmail(),"/queue/notification/error", errorNotification,header);
    }



}
