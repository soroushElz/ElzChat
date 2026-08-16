package com.example.ChatApplication.chat.models;

import com.example.ChatApplication.user.User;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name="chatMessage")
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE chatMessage SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private boolean forwarded=Boolean.FALSE;

  @ManyToOne
  @JoinColumn(name="author_Id")
  private User writer;


  private Long destinationId;

  private LocalDateTime timeSent;

  @Getter
  @Column(columnDefinition = "Text")
  private String content;

  @Column(name = "is_deleted")
  private boolean isDeleted =Boolean.FALSE;

  @Nullable
  @OneToOne(fetch=FetchType.LAZY)
  private ChatMessage replyTo;

  public ChatMessage() {

  }


  public ChatMessage(Long id) {
    this.id=id;
  }

  public ChatMessage(User writer, Long destinationChannelId, String content, @Nullable ChatMessage replyTo, LocalDateTime timeSent) {
    this.writer = writer;
    this.destinationId = destinationChannelId;
    this.content = content;
    this.replyTo=replyTo;
    this.timeSent=timeSent;

  }


  public ChatMessage(User writer, Long destinationChatId, String content,LocalDateTime datetime) {
    this.writer = writer;
    this.destinationId = destinationChatId;
    this.content = content;
    this.timeSent=datetime;
  }



}
