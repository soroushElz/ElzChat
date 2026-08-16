package com.example.ChatApplication.auth;


import com.example.ChatApplication.Email.EmailTemplateName;
import com.example.ChatApplication.Exception.OperationNotPermittedException;
import com.example.ChatApplication.Exception.TokenRefreshException;
import com.example.ChatApplication.Role.Role;
import com.example.ChatApplication.Role.RoleRepository;
import com.example.ChatApplication.Security.JwtService;
import com.example.ChatApplication.Security.RefreshToken;
import com.example.ChatApplication.Security.RefreshTokenService;
import com.example.ChatApplication.auth.DTO.AuthenticationRequest;
import com.example.ChatApplication.auth.DTO.AuthenticationResponse;
import com.example.ChatApplication.auth.DTO.RegistrationRequest;
import com.example.ChatApplication.auth.DTO.TokenRefreshRequest;
import com.example.ChatApplication.user.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;


    @Value("${application.mailing.activation-url}")
    private String activationUrl;

    @Value("${application.mailing.forget-password-url}")
    private String forgetPasswordUrl;


    public void register(RegistrationRequest registrationRequest) {

        String email= registrationRequest.getEmail();

        Optional<User> userOptional= userRepository.findByEmail(email);

        Optional<Role> UserRole=roleRepository.findByName("USER");

        if(userOptional.isPresent()){
                throw new OperationNotPermittedException("username is Already Taken");
            }

        var user=User.builder()
                .email(registrationRequest.getEmail())
                 .password(passwordEncoder.encode(registrationRequest.getPassword()))
                   .firstname(registrationRequest.getFirstname())
                     .lastname(registrationRequest.getLastname())
                       .roles(List.of(UserRole.get()))
                           .build();
        userRepository.save(user);

    }

    
    public AuthenticationResponse authenticate(AuthenticationRequest authenticationRequest) {
         var auth= authenticationManager.authenticate(
                                        new UsernamePasswordAuthenticationToken (
                                                authenticationRequest.getEmail(),
                                                 authenticationRequest.getPassword()));

     var Claims=new HashMap<String,Object>();
     var user=(User)auth.getPrincipal();
     Claims.put("fullname",user.getName());
     var jwt=jwtService.GenerateToken(Claims,(User)auth.getPrincipal());
      RefreshToken refreshToken= refreshTokenService.CreateRefreshToken(user.getId());
     return AuthenticationResponse.builder()
             .accessToken(jwt)
             .refreshToken(refreshToken.getToken())
             .build();

    }


    public AuthenticationResponse refreshToken(TokenRefreshRequest refreshRequest) throws TokenRefreshException{
        String  requestRefreshToken = refreshRequest.getRefreshToken();

        return refreshTokenService.findByToken(requestRefreshToken)
                 .map(refreshTokenService::verifyExpiration)
                  .map(RefreshToken::getUser)
                   .map(user -> {
                    String token= jwtService.GenerateToken(user);
                    return  AuthenticationResponse.builder()
                            .refreshToken(requestRefreshToken)
                            .accessToken(token)
                            .build();
                })

                .orElseThrow(() -> new TokenRefreshException(requestRefreshToken,
                        "Refresh token is not in database!"));
    }



}
