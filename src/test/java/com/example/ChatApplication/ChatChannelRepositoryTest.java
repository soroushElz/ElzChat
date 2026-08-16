package com.example.ChatApplication;

import com.example.ChatApplication.chat.ChatType;
import com.example.ChatApplication.chat.models.ChatChannel;
import com.example.ChatApplication.chat.repositories.ChatChannelRepository;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.UserRepository;
import com.example.ChatApplication.user.dtos.UserDto;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Set;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChatChannelRepositoryTest {

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
    public void test_find_Private_ChatChannel(){
       var user1 =User.builder().email("soroush.yz.97@gmail.com").build();
       var user2 =User.builder().email("soheil.yz.97@gmail.com").build();
       List<User> savedUsers= userRepository.saveAllAndFlush(List.of(user1,user2));
       var privateChannel = new ChatChannel();
      //////private chats have only 2 users
       privateChannel.addUser(savedUsers.get(0));
       privateChannel.addUser(savedUsers.get(1));
       privateChannel.setChatType(ChatType.PRIVATE_CHAT);
       var expectedChatChannel=chatChannelRepository.saveAndFlush(privateChannel);
       var actualChatChannel=chatChannelRepository.findPrivateChatChannel(user1.getId(),user2.getId());

        Assertions.assertThat(actualChatChannel.get(0))
                .usingRecursiveComparison()
                .isEqualTo(expectedChatChannel);
    }

    @Test
    @Rollback(value = false)
    public void test_getMembers_By_ChatId(){
        var savedUsers= userRepository.saveAllAndFlush(List.of(User.builder().email("soroush.yz.97@gmail.com").build(),
                User.builder().email("soheil.yz.97@gmail.com").build(),
                User.builder().email("sogand.yz.97@gmail.com").build()));
        var chatChannel=new ChatChannel();
        savedUsers.forEach(chatChannel::addUser);
        var savedChatChannel=chatChannelRepository.saveAndFlush(chatChannel);

        List<UserDto> actualMemberDTOs = chatChannelRepository.getMembersByChatId(savedChatChannel.getId());
        List<UserDto> expectedMemberDTOs = savedUsers.stream().map(user-> new UserDto(user.getId(),user.getUsername())).toList();

        Assertions.assertThatList(actualMemberDTOs)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .ignoringActualNullFields()
                .isEqualTo(expectedMemberDTOs);
    }
}
