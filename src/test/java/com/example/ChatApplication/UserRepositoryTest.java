package com.example.ChatApplication;

import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.chat.repositories.ChatChannelRepository;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Set;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    ChatChannelRepository chatChannelRepository;

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;
    @BeforeEach
    public void init(){
        entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getSchemaManager().truncateMappedObjects();
    }
    @Test
    @Rollback(false)
    public void test_findUserWith_BlockedList_ById(){
      var user1= userRepository.saveAndFlush( User.builder().email("soroush.yz.97@gmail.com").build());
      var user2= userRepository.saveAndFlush(User.builder().email("soheil.yz.97@gmail.com").build());
      var expectedUser3= userRepository.saveAndFlush(User.builder().email("sogand.yz.97@gmail.com").blockedUsers(Set.of(user1,user2)).build());
      var actualUser3WithBlockedList=userRepository.findUserWithBlockedListById(expectedUser3.getId());

      assertThatCollection(actualUser3WithBlockedList.get().getBlockedUsers()).usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(Set.of(user1,user2));
        assertEquals(expectedUser3.getId(),actualUser3WithBlockedList.get().getId());

    }

    @Test
    @Rollback(value = false)
    public void test_find_user_withChats(){

        var user1= userRepository.saveAndFlush( User.builder().email("soroush.yz.97@gmail.com").build());
        var channel=new ChatChannel();
        channel.addUser(user1);
        var savedChannel =chatChannelRepository.save(channel);

        var actualUserWithChats= userRepository.findUserWithChats(user1.getId());
        assertNotNull(actualUserWithChats.get().getChats());
        assertThatCollection(actualUserWithChats.get().getChats()).usingRecursiveComparison().
                  ignoringCollectionOrder()
                .isEqualTo(Set.of(savedChannel));
    }




}
