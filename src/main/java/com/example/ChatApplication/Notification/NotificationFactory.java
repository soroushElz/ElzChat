package com.example.ChatApplication.Notification;

import com.example.ChatApplication.user.dtos.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class NotificationFactory {

    private final Map<Class<?>,INotificationBuilder<?>> notificationBuilders=new HashMap<>();
    @Autowired
    public NotificationFactory(List<INotificationBuilder<?>> notificationBuilderList ){
     for(INotificationBuilder<?> builder : notificationBuilderList){
         notificationBuilders.put(builder.getPayloadType(),builder);
     }

    }
    @SuppressWarnings("unchecked")
    <T extends INotificationPayload> NotificationDto create(T payload, UserDto recipient){
       INotificationBuilder<T> builder= (INotificationBuilder<T>) notificationBuilders.get(payload.getClass());
       if (builder==null) {
           throw new IllegalArgumentException("No builder for: " + payload.getClass());
       }
        return builder.create(payload,recipient);
    }


}
