package com.example.ChatApplication.Notification;

import com.example.ChatApplication.Commons.BaseEntity;
import com.example.ChatApplication.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;

import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Notification extends BaseEntity {
    private Long recipientId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType notificationType;
    @Enumerated(EnumType.STRING)
    private Status status;
    @Type(JsonType.class)
    @Convert(converter = PayloadConverter.class)
    @Column(columnDefinition = "JSONB")
    private INotificationPayload notificationPayload;
    private LocalDateTime timeDelivered;

}
