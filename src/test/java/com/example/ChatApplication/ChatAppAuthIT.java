package com.example.ChatApplication;
import com.example.ChatApplication.Handler.ExceptionResponse;
import com.example.ChatApplication.Role.Role;
import com.example.ChatApplication.Role.RoleRepository;
import com.example.ChatApplication.auth.DTO.AuthenticationRequest;
import com.example.ChatApplication.auth.DTO.AuthenticationResponse;
import com.example.ChatApplication.auth.DTO.RegistrationRequest;
import com.example.ChatApplication.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.PersistenceUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.*;
import java.util.List;
import java.util.Map;

import static com.example.ChatApplication.Handler.BusinessErrorCodes.BAD_CREDENTIALS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;


@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ChatAppAuthIT {
    Logger logger = LogManager.getLogger();
    @Autowired
    private TestRestTemplate testRestTemplate;
    @Autowired
    TransactionTemplate transactionTemplate;
    public static String  jwtToken="vd";

    @PersistenceUnit
    EntityManagerFactory entityManagerFactory;

    @Autowired
    RoleRepository roleRepository;
    @PersistenceContext
    private  EntityManager entityManager;

    @Value("${server.port}")
    private  int port;
    private String host_URL;
    @BeforeEach
    public void init(){
        entityManagerFactory.unwrap(SessionFactoryImplementor.class)
                .getSchemaManager().truncateMappedObjects();
      Role role=Role.builder()
                    .name("USER").build();

       roleRepository.save(role);
        host_URL="http://localhost:"+port+"/api/v1";
    }


    @Test
    public void testCreateUser_WhenValidDetailsProvided() throws JsonProcessingException {
        var registrationRequest = RegistrationRequest.builder().firstname("Soroush").lastname("yahyazadeh")
                .email("Soroush.yz.97@gmail.com").password("12345678")
                .build();

        ObjectMapper objectMapper = new ObjectMapper();

        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<String>(objectMapper.writeValueAsString(registrationRequest),headers);

        ResponseEntity<Map> response=testRestTemplate.postForEntity(host_URL+"/auth/register",
                entity,
                Map.class
        );
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertThat((Map<String, String>) response.getBody()).
                   containsEntry("message","registered successfully" );

    }


    @Test
    public void testReceiveJWT_WhenLoginIsSuccessful() throws JSONException {
         createNewUser();
        var jsonAuthenticationRequest= new JSONObject();
        jsonAuthenticationRequest.put("email","soroush.yz.97@gmail.com");
        jsonAuthenticationRequest.put("password","12345678");
        var headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        ResponseEntity<AuthenticationResponse> response=testRestTemplate.postForEntity(host_URL+"/auth/authenticate",
               new HttpEntity<String>(jsonAuthenticationRequest.toString(),headers) ,AuthenticationResponse.class);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertNotNull(response.getBody().getAccessToken());
        assertNotNull(response.getBody().getRefreshToken());

    }

    private void createNewUser() {


        transactionTemplate.executeWithoutResult(transactionStatus -> {
            Role role = entityManager.createQuery("from Role r where r.name= :n ", Role.class)
                    .setParameter("n", "USER").getSingleResult();

            var user = User.builder()
                    .email("soroush.yz.97@gmail.com")
                        .password(new BCryptPasswordEncoder().encode("12345678"))
                          .firstname("soroush")
                          .lastname("yahyazadeh")
                           .roles(List.of(role))
                           .build();

            entityManager.persist(user);
        });
    }

   @Test
    public void testAuthenticationFailed() {
       RestClient restClient = RestClient.builder().baseUrl(host_URL).build();

       HttpStatusCode statusCode = null;
       ExceptionResponse exceptionBody = null;
       try {
           ResponseEntity<String> response = restClient.post()
                   .uri("/auth/authenticate")
                   .contentType(MediaType.APPLICATION_JSON)
                   .body(AuthenticationRequest.builder().email("soroush.yz.97").password("12344")
                                                  .build())
                   .retrieve()
                   .toEntity(String.class);
       } catch (HttpStatusCodeException e) {
           exceptionBody = e.getResponseBodyAs(ExceptionResponse.class);
           statusCode = e.getStatusCode();
       }

       assert statusCode != null;
       assertTrue(statusCode.isSameCodeAs(HttpStatusCode.valueOf(401)));

       assertThat(exceptionBody).usingRecursiveComparison()
               .isEqualTo(ExceptionResponse.builder()
                       .businessErrorCode(BAD_CREDENTIALS.getCode())
                       .businessErrorCodeDescription(BAD_CREDENTIALS.getDescription())
                       .error("Login and / or Password is incorrect")
                       .build()
               )

       ;

   }



   }





