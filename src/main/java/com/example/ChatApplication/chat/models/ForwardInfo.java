package com.example.ChatApplication.chat.models;

import com.example.ChatApplication.user.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name ="forwardInfo" )
@Builder
@SQLDelete(
        sql = "UPDATE forwardInfo SET is_deleted = true WHERE id = ?"
)
@SQLRestriction("is_deleted=false")
public class ForwardInfo {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY )
    private Long id;
    private Long originalMessageId;
    private Long originalSenderId;
    private Long forwardedMessageId;
    private String originalMessageTextSnapShot;
    private Long senderId;
    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted=Boolean.FALSE;



}
