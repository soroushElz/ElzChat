package com.example.ChatApplication.chat.models;

import com.example.ChatApplication.user.User;
import jakarta.persistence.*;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

@Entity
@Table(name="chatMessage")
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne
  @JoinColumn(name="authorUserId")
  private User writer;

  @OneToOne
  @JoinColumn(name="recipientUserId")
  private User reciever;

  @NotNull
  private Date timeSent;

  @NotNull
  private String contents;

  public ChatMessage() {

  }

  public Long getId() {
    return id;
  }


  public User getWriter() {
    return writer;
  }

  public void setWriter(User writer) {
    this.writer = writer;
  }

  public User getReciever() {
    return reciever;
  }

  public void setReciever(User reciever) {
    this.reciever = reciever;
  }

  public Date getTimeSent() {
    return timeSent;
  }

  public String getContents() {
    return contents;
  }

  public ChatMessage(User writer, User reciever, String contents) {
    this.writer = writer;
    this.reciever = reciever;
    this.contents = contents;
  }
}
