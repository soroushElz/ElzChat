package com.example.ChatApplication.user;

import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.chat.models.ChatMessage;
import com.example.ChatApplication.user.dtos.UserDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String username);

    @Modifying
    @Transactional
    void deleteByEmail(String email);



    @Query(" FROM " +
            "User u  left join fetch u.blockedUsers blocked " +
            "where" +
            " u.id=:userId")
    Optional<User> findUserWithBlockedListById(@Param("userId") Long userId);

    @Query(" FROM User u" +
            " join fetch u.chats c" +
            " where u.id = :userId"
            )
    Optional<User> findUserWithChats(@Param("userId")Long id);

    Set<User> findByIdIsIn(List<Long> userIds);


}
