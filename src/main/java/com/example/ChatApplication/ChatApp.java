package com.example.ChatApplication;

import com.example.ChatApplication.Role.Role;
import com.example.ChatApplication.Role.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;

@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
public class ChatApp implements CommandLineRunner   {

    @Autowired
    private RoleRepository roleRepository;

    public static void main(String[] args)   {
        SpringApplication.run(ChatApp.class, args);

    }

    @Override
    public void run(String... args) throws Exception {
       if (roleRepository.findByName("USER").isEmpty()) {
           roleRepository.save(Role.builder()
                          .name("USER").build()
           );
       }
    }

    @Autowired
    private ObjectMapper objectMapper;



    @PostConstruct
    public void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
    }

}
