package com.example.ChatApplication.chat.repositories;

import com.example.ChatApplication.chat.models.ReactionAggregate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReactionAggregateRepository extends JpaRepository<ReactionAggregate, ReactionAggregate.ReactionCompositeKey> {
}
