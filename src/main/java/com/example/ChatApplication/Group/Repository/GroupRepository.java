package com.example.ChatApplication.Group.Repository;

import com.example.ChatApplication.Group.Entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group,Long> {


    Optional<Group> findByChatChannelId(Long ChatChannelId);
}
