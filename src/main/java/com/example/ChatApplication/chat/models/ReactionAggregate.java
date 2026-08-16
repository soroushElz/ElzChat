package com.example.ChatApplication.chat.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ReactionAggregate {
    @EmbeddedId
    ReactionCompositeKey id;

    Integer count;

    @Override
    public String toString() {
        return "ReactionAggregate{" +
                "id=" + id +
                ", count=" + count +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    public void addCount(){
        count++;
    }
    public void subtractCount(){
        count--;
    }

    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;

    public ReactionAggregate(ReactionCompositeKey id) {
        count=1;
        this.id=id;
    }

    @Embeddable
    @Getter
    @AllArgsConstructor
    public static class ReactionCompositeKey implements Serializable{
        Long messageId;
        @Enumerated(EnumType.STRING)
        ReactionType type;

        public ReactionCompositeKey() {

        }
    }

}

