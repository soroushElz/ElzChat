package com.example.ChatApplication;
import com.example.ChatApplication.chat.models.ChatMessage;
import com.example.ChatApplication.chat.repositories.ChatMessageRepository;
import com.example.ChatApplication.chat.repositories.FieldFilter;
import com.example.ChatApplication.chat.repositories.Operation;
import com.example.ChatApplication.chat.repositories.SearchFilters;
import com.example.ChatApplication.chat.services.ChatService;
import com.example.ChatApplication.user.User;
import com.example.ChatApplication.user.UserRepository;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.random.RandomGenerator;
import java.util.stream.Stream;



@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class ChatMessageRepositoryTest {

    @Autowired
    ChatMessageRepository messageRepository;
    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    public void init(){
        entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getSchemaManager().truncateMappedObjects();
    }


    @Test
    @Rollback(value=false)
    public void test_findAll_by_in_timePeriod(){
      List<ChatMessage> testChatMessage= generateTestChatMessages();
        var savedTestMessages= messageRepository.saveAllAndFlush(testChatMessage);

        var randomDateStart= LocalDateTime.now().minusHours(RandomGenerator.getDefault().nextLong(1000)).toLocalDate();
        var periodEnd=randomDateStart.plusDays(RandomGenerator.getDefault().nextLong(
                ChronoUnit.DAYS.between(randomDateStart,LocalDate.now()))+1L);
      
        var expectedMessages= savedTestMessages.stream()
                .filter(message->message.getTimeSent().isAfter(randomDateStart.atStartOfDay())
                        && message.getTimeSent().isBefore(periodEnd.atStartOfDay())
                        &&  Objects.equals(message.getDestinationId(), 1L))
                .toList();

       var afterDateFilter= new SearchFilters(FieldFilter.TIME_SENT,randomDateStart.toString(), Operation.GT);
       var beforeDateFilter=new SearchFilters(FieldFilter.TIME_SENT,periodEnd.toString(),Operation.LT);
       var spec=ChatService.getChatMessageSpecification(List.of(afterDateFilter,beforeDateFilter));

       List<ChatMessage> actualMessages= messageRepository.findAll(spec,Pageable.unpaged()).stream()
               .filter(m->m.getDestinationId()==1L).toList();

               Assertions.assertThatList(actualMessages)
                       .usingRecursiveComparison()
                       .ignoringCollectionOrder()
                       .isEqualTo(expectedMessages);
    }
    @Test
    @Rollback(value=false)
    public void test_findAll_during_day(){
        List<ChatMessage> testChatMessages= generateTestChatMessages();
        var savedMessages=messageRepository.saveAllAndFlush(testChatMessages);
        var randomDate= LocalDateTime.now().minusHours(RandomGenerator.getDefault().nextLong(1000)).toLocalDate();
        var expectedMessages= savedMessages.stream().filter(message->message.getTimeSent().isAfter(randomDate.atStartOfDay())
                                                                && message.getTimeSent().isBefore(randomDate.atStartOfDay().plusDays(1))
                                                                &&  Objects.equals(message.getDestinationId(), 1L))
                                                                    .toList();
        var duringDayFilter=new SearchFilters(FieldFilter.TIME_SENT,randomDate.toString(),Operation.DURING);
        var spec=ChatService.getChatMessageSpecification(List.of(duringDayFilter));
        List<ChatMessage> actualMessages= messageRepository.findAll(spec,Pageable.unpaged()).stream()
                .filter(m->m.getDestinationId()==1L).toList();

        Assertions.assertThatList(actualMessages)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedMessages);
    }

    @Test
    @Rollback(false)
    public void test_search_message_by_writer_id(){
        List<ChatMessage> testMessages = generateTestChatMessages();
        List<User> users=new ArrayList<>();
        for(int i=0;i<4;i++){
            users.add(userRepository.saveAndFlush(new User()));
        }
        testMessages.forEach(message->message.setWriter(users.get(ThreadLocalRandom.current().nextInt(users.size()))));
        List<ChatMessage> savedTestMessages= messageRepository.saveAllAndFlush(testMessages);
        User user = users.get(ThreadLocalRandom.current().nextInt(users.size()));

        var expectedMessages= savedTestMessages.stream()
                .filter(message->
                Objects.equals(message.getWriter().getId(), user.getId())
                &&  Objects.equals(message.getDestinationId(), 1L)).toList();
        SearchFilters userSearchFilter= new SearchFilters(FieldFilter.WRITER_ID,String.valueOf(user.getId()),Operation.EQ);
        var spec=ChatService.getChatMessageSpecification(List.of(userSearchFilter));
        List<ChatMessage> actualMessages= messageRepository.findAll(spec,Pageable.unpaged()).stream()
                .filter(m->m.getDestinationId()==1L).toList();

        Assertions.assertThatList(actualMessages)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedMessages);

    }


    @Test
    @Rollback(false)
    public void test_search_by_message_content(){
       List<ChatMessage> testMessages=generateTestChatMessages();
       String[] sampleTexts ={"ewtgd","grbhg","xcdwr"};

       testMessages.forEach(chatMessage -> chatMessage.setContent(sampleTexts[RandomGenerator.getDefault().nextInt(sampleTexts.length)]));

       List<ChatMessage> savedTestMessages= messageRepository.saveAllAndFlush(testMessages);

       String searchString=sampleTexts[RandomGenerator.getDefault().nextInt(sampleTexts.length)];

       var expectedMessages=savedTestMessages.stream().filter(message->message.getContent().contains(searchString)
        &&  Objects.equals(message.getDestinationId(), 1L)
         ).toList();

       SearchFilters textSearchFilter= new SearchFilters(FieldFilter.CONTENT,searchString,Operation.LIKE);

        var spec=ChatService.getChatMessageSpecification(List.of(textSearchFilter));
        List<ChatMessage> actualMessages= messageRepository.findAll(spec,Pageable.unpaged()).stream()
                .filter(m->m.getDestinationId()==1L).toList();

        Assertions.assertThatList(actualMessages)
                .usingRecursiveComparison()
                .ignoringCollectionOrder()
                .isEqualTo(expectedMessages);
    }
    private static List<ChatMessage> generateTestChatMessages() {
        return     Stream.generate(() ->
                        ChatMessage.builder()
                                .destinationId(ThreadLocalRandom.current().nextLong(1L, 3L))
                                .timeSent(LocalDateTime.now().minusHours(RandomGenerator.getDefault().nextLong(1000)))
                                .build()
                )
                .limit(50).toList();
    }
}

