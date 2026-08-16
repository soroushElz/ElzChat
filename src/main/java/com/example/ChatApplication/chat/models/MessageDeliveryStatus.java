package com.example.ChatApplication.chat.models;

import com.example.ChatApplication.Commons.BaseEntity;
import com.example.ChatApplication.Notification.Status;
import com.example.ChatApplication.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "delivery_status")
public class MessageDeliveryStatus extends BaseEntity {
    @Enumerated(EnumType.STRING)
    private Status deliveryStatus;

    @ManyToOne
    private User recipient;

    @ManyToOne
    private ChatMessage message;

}
